package com.se.working.invigilation.service;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.se.working.exception.SEWMException;
import com.se.working.invigilation.dao.CourseDao;
import com.se.working.invigilation.dao.CourseSectionDao;
import com.se.working.invigilation.dao.InviDao;
import com.se.working.invigilation.dao.InviInfoDao;
import com.se.working.invigilation.dao.InviTypeDao;
import com.se.working.invigilation.dao.TeacherInviDao;
import com.se.working.invigilation.entity.Course;
import com.se.working.invigilation.entity.CourseSection;
import com.se.working.invigilation.entity.Invigilation;
import com.se.working.invigilation.entity.InvigilationInfo;
import com.se.working.invigilation.entity.InvigilationStatusType;
import com.se.working.invigilation.entity.TeacherInvigilation;
import com.se.working.invigilation.entity.InvigilationStatusType.InviStatusType;
import com.se.working.message.AlidayuMessage;
import com.se.working.service.GenericService;
import com.se.working.task.entity.FileTask;
import com.se.working.task.entity.FileTaskDetail;
import com.se.working.task.service.TaskService;
import com.se.working.util.DateUtils;
import com.se.working.util.FileTaskUtils;
import com.se.working.util.InviExcelUtil;
import com.se.working.util.TimetableExcelUtil;

/**
 * 监考相关业务逻辑处理
 * 
 * @author BO
 *
 */
@Service
@Transactional
public class InviService extends GenericService<Invigilation, Long> {
	@Autowired
	private InviInfoDao inviInfoDao;
	@Autowired
	private InviTypeDao inviTypeDao;
	@Autowired
	private TeacherInviDao teacherInviDao;
	@Autowired
	private CourseDao courseDao;
	@Autowired
	private CourseSectionDao courseSectionDao;
	@Autowired
	private InviDao inviDao;
	@Autowired
	private AlidayuMessage alidayuMessage;
	@Autowired
	private TaskService taskService;
	@Autowired
	private InviTimer inviTimer;

	/**
	 * 
	 * @return 用户监考信息
	 */
	public List<TeacherInvigilation> findTeacherInvigilations() {
		return teacherInviDao.list();
	}

	/**
	 * 单独导入课表文件，如果数据库中已有则清空原课表。返回课表集合
	 * 
	 * @param uploadFile
	 * @return
	 * @throws IOException
	 * @throws SEWMException
	 */
	public List<Course> importTimetable(MultipartFile uploadFile) {
		String name = null;
		List<Course> courses = null;
		try {
			InputStream is = uploadFile.getInputStream();
			name = TimetableExcelUtil.getTimetableName(is);
			if (name == null) {
				throw new SEWMException("不是课表文件，" + uploadFile.getOriginalFilename());
			}
			TeacherInvigilation teacher = null;
			for (TeacherInvigilation t : teacherInviDao.list()) {
				if (t.getUser().getName().equals(name)) {
					teacher = t;
				}
			}
			if (teacher == null) {
				throw new SEWMException(name + "非本专业教师，" + uploadFile.getOriginalFilename());
			}
			// 如果已经导入过课表，则首先清空
			if (teacher.getCourses().size() > 0) {
				for (Course course : teacher.getCourses()) {
					// 级联清空相应CourseSection
					courseDao.delete(course);
				}
			}
			courses = TimetableExcelUtil.getExcel(is);
			for (Course course : courses) {
				course.setTeacher(teacher);
				// 有级联，但是关系由many维护，因此在保存时需在session中使many重新set one端
				for (CourseSection cs : course.getCourseSections()) {
					cs.setCourse(course);
				}
				courseDao.persist(course);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			uploadFile = null;
		}

		return courses;
	}

	/**
	 * 查询所有课程
	 * 
	 * @return
	 */
	public List<Course> findCourses() {
		return courseDao.list();
	}

	/**
	 * 导入课表文件任务
	 * 
	 * @param filetaskId
	 * @return
	 * @throws SEWMException
	 */
	/*
	 * public List<Course> importTimetableTask(long filetaskId) { List<Course>
	 * courses = new ArrayList<>(); FileTask task =
	 * taskService.findById(filetaskId);
	 * 
	 * for (FileTaskDetail d : task.getFileTaskDetails()) { if (d.getFile() !=
	 * null) { File file =
	 * FileTaskUtils.getOrCreateFileTaskFile(task.getDirectory(), d.getFile());
	 * courses.addAll(readTimetable(file)); } } return courses; }
	 */

	/**
	 * 导入监考信息
	 * 
	 * @param excelFile
	 * @return
	 * @throws SEWMException
	 * @throws Exception
	 */
	public List<InvigilationInfo> importInvi(MultipartFile uploadFile) {
		List<InvigilationInfo> newInfos = new ArrayList<>();
		try {
			InputStream is = uploadFile.getInputStream();
			// 封装监考人数，地点，起止时间
			List<InvigilationInfo> infos = InviExcelUtil.getExcel(is);
			if (infos == null) {
				throw new SEWMException("未读取到专业相关监考信息");
			}
			List<InvigilationInfo> oldInfos = inviInfoDao.list();
			for (InvigilationInfo i : infos) {
				InvigilationInfo inviInfo = null;
				boolean exists = false;
				for (InvigilationInfo o : oldInfos) {
					// 判断条件，同一时间同一地点不可能有2个监考，有则视为已存在
					if (i.getLocation().equals(o.getLocation())
							&& i.getStartTime().getTime().toString().equals(o.getStartTime().getTime().toString())) {

						// 如果监考人数发生变化，重置监考人数，并删除原监考安排
						if (o.getRequiredNumber() != i.getRequiredNumber()) {
							o.setRequiredNumber(i.getRequiredNumber());
							// 重置状态
							o.setCurrentStatusType(new InvigilationStatusType(InviStatusType.UNASSIGNED));
							for (Invigilation invi : o.getInvigilations()) {
								inviDao.delete(invi);
							}
							// 先刷新，在同步
							inviInfoDao.flush();
							inviInfoDao.refresh(o);
						}
						inviInfo = o;
						exists = true;
					}
				}
				// 不存在则保存
				if (!exists) {
					// 持久化时需关联延迟加载对象的创建
					i.setCurrentStatusType(new InvigilationStatusType(InviStatusType.UNASSIGNED));
					inviInfoDao.persist(i);
					// 先刷新，在同步
					inviInfoDao.flush();
					inviInfoDao.refresh(i);
					inviInfo = i;
				}
				newInfos.add(inviInfo);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return newInfos;
	}

	/**
	 * 返回指定教师的全部监考信息
	 * 
	 * @param userId
	 * @return
	 */
	public List<InvigilationInfo> findInviInfosByUserId(long userId) {
		List<InvigilationInfo> infos = new ArrayList<>();
		for (Invigilation i : teacherInviDao.get(userId).getInvigilations()) {
			infos.add(i.getInvInfo());
		}
		return infos;
	}

	/**
	 * 返回指定教师、指定监考状态的所有监考信息
	 * 
	 * @param userId
	 * @param typeId
	 * @return
	 */
	public List<InvigilationInfo> findInvisByUserIdAndTypeId(long userId, long typeId) {
		List<InvigilationInfo> infos = new ArrayList<>();
		for (Invigilation i : inviDao.listInvisByUserIdAndTypeId(userId, typeId)) {
			infos.add(i.getInvInfo());
		}
		return infos;
	}

	/**
	 * 查找相应状态全部监考信息
	 * 
	 * @return
	 */
	public List<InvigilationInfo> findInviInfosByTypeId(long inviTypeId) {
		return new ArrayList<>(inviTypeDao.get(inviTypeId).getInvInfo());
	}

	/**
	 * 基于指定监考信息状态，分页查询
	 * 
	 * @param inviTypeId
	 * @param firstResult
	 * @param maxResults
	 * @return
	 */
	public List<InvigilationInfo> findInviInfosByTypeId(long inviTypeId, int firstResult, int maxResults) {
		return inviInfoDao.listInviInfos(inviTypeId, firstResult, maxResults);
	}

	/**
	 * 返回全部监考信息
	 * 
	 * @return
	 */
	public List<InvigilationInfo> findAllInviInfos() {
		return new ArrayList<>(inviInfoDao.list());
	}

	/**
	 * 返回全部监考信息，分页
	 * 
	 * @return
	 */
	public List<InvigilationInfo> findAllInviInfos(int firstResult, int maxResults) {
		return new ArrayList<>(inviInfoDao.list(firstResult, maxResults));
	}

	/**
	 * 返回指定监考信息
	 * 
	 * @param inviId
	 * @return
	 */
	public InvigilationInfo findInviInfo(long inviId) {
		return inviInfoDao.get(inviId);
	}

	/**
	 * 查找授课时间冲突教师，与监考关闭、通知关闭无关
	 * 
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	private List<TeacherInvigilation> findSectionConflicts(Calendar startTime, Calendar endTime) {
		List<CourseSection> sections = courseSectionDao.listSections(startTime, endTime);
		List<TeacherInvigilation> teachers = new ArrayList<>();
		for (CourseSection cs : sections) {
			teachers.add(cs.getCourse().getTeacher());
		}
		return teachers;
	}

	/**
	 * 查找监考时间冲突教师，与监考关闭、通知关闭无关
	 * 
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	private List<TeacherInvigilation> findInviConfilicts(Calendar startTime, Calendar endTime) {
		List<InvigilationInfo> infos = inviInfoDao.listInviInfos(startTime, endTime);
		List<TeacherInvigilation> teachers = new ArrayList<>();
		for (InvigilationInfo info : infos) {
			for (Invigilation i : info.getInvigilations()) {
				teachers.add(i.getTeacher());
			}
		}
		return teachers;
	}

	/**
	 * 整合监考时间冲突，与授课时间冲突，返回拼接后的字符串
	 * 
	 * @param info
	 * @return
	 */
	public Map<Long, String> findConflicts(long inviId) {
		InvigilationInfo inviInfo = inviInfoDao.get(inviId);
		Map<Long, String> map = new LinkedHashMap<>();
		if (inviInfo == null) {
			return map;
		}
		// 监考与授课时间冲突教师
		List<TeacherInvigilation> sectionConfs = findSectionConflicts(inviInfo.getStartTime(), inviInfo.getEndTime());
		// 监考与监考时间冲突教师
		List<TeacherInvigilation> inviConfis = findInviConfilicts(inviInfo.getStartTime(), inviInfo.getEndTime());
		// 去除相同教师
		Set<TeacherInvigilation> teachers = new LinkedHashSet<>();
		teachers.addAll(sectionConfs);
		teachers.addAll(inviConfis);
		// 添加ID与姓名
		for (TeacherInvigilation t : teachers) {
			map.put(t.getId(), t.getUser().getName() + "; ");
		}

		return setDefaultInviMap(inviId, map);
	}

	/**
	 * 查找推荐关闭教师，返回拼接字符串
	 * 
	 * @param inviId
	 * @return
	 */
	public Map<Long, String> findUnRCDs(long inviId) {
		List<TeacherInvigilation> unRCDs = teacherInviDao.listUnRDCs();
		Map<Long, String> map = new LinkedHashMap<>();
		for (TeacherInvigilation t : unRCDs) {
			map.put(t.getId(), t.getUser().getName() + "; ");
		}

		return setDefaultInviMap(inviId, map);
	}

	/**
	 * 查找所有可推荐监考教师，即非推荐关闭与通知关闭
	 * 
	 * @param inviId
	 * @return
	 */
	public Map<Long, String> findRCDs(long inviId) {
		Map<Long, String> map = new LinkedHashMap<>();
		List<TeacherInvigilation> rcds = teacherInviDao.listRDCs();
		for (TeacherInvigilation t : rcds) {
			map.put(t.getId(), t.getUser().getName() + "; ");
		}

		return setDefaultInviMap(inviId, map);
	}

	/**
	 * 完成对监考当日信息的封装
	 * 
	 * @param inviId
	 * @param map
	 * @return
	 */
	private Map<Long, String> setDefaultInviMap(long inviId, Map<Long, String> map) {
		InvigilationInfo inviInfo = inviInfoDao.get(inviId);
		if (inviInfo == null) {
			return map;
		}
		// 当日所有课程片段
		List<CourseSection> courseSections = courseSectionDao.listSectionsByDate(inviInfo.getStartTime());
		// 当日所有监考，当天00:00至次日00:00
		Calendar cDate = Calendar.getInstance();
		cDate.setTime(inviInfo.getStartTime().getTime());
		cDate.set(Calendar.HOUR_OF_DAY, 0);
		cDate.set(Calendar.MINUTE, 0);
		Calendar nDate = Calendar.getInstance();
		nDate.setTime(cDate.getTime());
		nDate.add(Calendar.DAY_OF_MONTH, 1);
		List<InvigilationInfo> inviInfos = inviInfoDao.listInviInfos(cDate, nDate);

		SimpleDateFormat sfDate = new SimpleDateFormat("MM-dd");
		SimpleDateFormat sfTime = new SimpleDateFormat("HH:mm");
		// 添加当日授课信息
		for (CourseSection c : courseSections) {
			if (map.get(c.getCourse().getTeacher().getId()) != null) {
				String string = map.get(c.getCourse().getTeacher().getId());
				string = string + c.getCourse().getName() + " " + c.getCourse().getLocation() + " "
						+ sfDate.format(c.getStartTime().getTime()) + " " + sfTime.format(c.getStartTime().getTime())
						+ "-" + sfTime.format(c.getEndTime().getTime()) + "; ";
				map.put(c.getCourse().getTeacher().getId(), string);
			}
		}
		// 添加当日监考信息
		for (InvigilationInfo info : inviInfos) {
			for (Invigilation i : info.getInvigilations()) {
				if (map.get(i.getTeacher().getId()) != null) {
					String string = map.get(i.getTeacher().getId()) + "监考 " + info.getLocation() + " ";
					string = string + sfDate.format(info.getStartTime().getTime()) + " "
							+ sfTime.format(info.getStartTime().getTime()) + "-"
							+ sfTime.format(info.getEndTime().getTime()) + "; ";
					map.put(i.getTeacher().getId(), string);
				}
			}
		}
		// 添加监考次数
		for (TeacherInvigilation t : teacherInviDao.list()) {
			if (map.get(t.getId()) != null) {
				String string = map.get(t.getId());
				string = string + t.getInvigilations().size() + "; ";
				map.put(t.getId(), string);
			}

		}
		return map;
	}

	/**
	 * 直接更新交复杂。如果第一次人数为2人，后修改为3人，则需创建一个新对象，因此选择删除原记录，创建新纪录
	 * 
	 * @param inviInfoId
	 * @param tIds
	 */
	public InvigilationInfo addinvi(long inviInfoId, long[] tIds) {
		InvigilationInfo info = inviInfoDao.get(inviInfoId);
		// 删除原记录
		for (Invigilation i : info.getInvigilations()) {
			inviDao.delete(i);
		}
		// 重新创建记录
		for (int i = 0; i < tIds.length; i++) {
			Invigilation invigilation = new Invigilation();
			invigilation.setInvInfo(info);
			invigilation.setTeacher(teacherInviDao.get(tIds[i]));
			inviDao.persist(invigilation);
			info.setCurrentStatusType(new InvigilationStatusType(InviStatusType.ASSIGNED));
		}
		inviInfoDao.flush();
		inviInfoDao.refresh(info);
		return info;
	}

	/**
	 * 发送监考分配短信
	 * 
	 * @param info
	 */
	public List<String> sendInviNoticeMessage(long[] inviids) {
		List<Invigilation> invigilations = new ArrayList<>();
		for (int i = 0; i < inviids.length; i++) {
			invigilations.add(inviDao.get(inviids[i]));
		}
		return alidayuMessage.sendInviNotice(invigilations);
	}

	/**
	 * 更新监考信息
	 * 
	 * @param newInfo
	 */
	public void updateInviInfo(InvigilationInfo newInfo) {
		InvigilationInfo info = inviInfoDao.get(newInfo.getId());
		info.setComment(newInfo.getComment());
		info.setStartTime(newInfo.getStartTime());
		info.setEndTime(newInfo.getEndTime());
		// 如果人数发生变化，将监考状态变为未分配
		if (newInfo.getRequiredNumber() != info.getRequiredNumber()) {
			info.setCurrentStatusType(new InvigilationStatusType(InviStatusType.UNASSIGNED));
		}

		info.setRequiredNumber(newInfo.getRequiredNumber());
		info.setLocation(newInfo.getLocation());
		info.setComment(newInfo.getComment());
	}

	/**
	 * 删除监考信息
	 * 
	 * @param inviInfoId
	 */
	public void deleteInviInfo(long inviInfoId) {
		inviInfoDao.delete(inviInfoDao.get(inviInfoId));
	}

	/**
	 * 添加监考信息
	 * 
	 * @param info
	 */
	public long addInviInfo(InvigilationInfo info) {
		info.setCurrentStatusType(new InvigilationStatusType(InviStatusType.UNASSIGNED));
		inviInfoDao.persist(info);
		inviInfoDao.flush();
		inviInfoDao.refresh(info);
		return info.getId();
	}

	/**
	 * 发送监考提醒短信
	 */
	public void sendInviRemind() {
		inviTimer.inviRemind();
	}

	/**
	 * 将从学期初至今的已分配监考置为完成状态
	 */
	public void setInviInfoDone() {
		Calendar cDate = Calendar.getInstance();
		cDate.setTime(new Date());
		List<InvigilationInfo> infos = inviInfoDao.listInviInfos(DateUtils.getBaseCalender(), cDate,
				InviStatusType.ASSIGNED);
		if (infos != null) {
			for (InvigilationInfo i : infos) {
				i.setCurrentStatusType(new InvigilationStatusType(InviStatusType.DONE));
			}
		}
	}

	/**
	 * 
	 */
	public ResponseEntity<byte[]> downloadInviInfoExcel() {
		List<InvigilationInfo> infos = findAllInviInfos();
		byte[] datas = InviExcelUtil.createInviDetailExcel(infos, findTeacherInvigilations());
		SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
		String fileName = "监考记录-" + date.format(new Date()) + ".xlsx";
		ResponseEntity<byte[]> entity = FileTaskUtils.toResponseEntity(fileName, datas);
		return entity;
	}

}

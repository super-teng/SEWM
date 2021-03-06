package com.se.working.invigilation.dao;

import java.util.Calendar;
import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import com.se.working.dao.GenericDao;
import com.se.working.invigilation.entity.InvigilationInfo;

@Repository
public class InviInfoDao extends GenericDao<InvigilationInfo, Long> {
	/**
	 * 基于时间查找相应监考，全部监考状态<br>
	 * 查询条件：开始时间在监考时间内，或，结束时间在监考时间内，或，开始时间在监考时间前同时结束时间在监考时间后
	 * 
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<InvigilationInfo> listInviInfos(Calendar startTime, Calendar endTime) {
		String HQL = "FROM InvigilationInfo i WHERE (:st>=i.startTime AND :st<=i.endTime) "
				+ "OR (:et>=i.startTime AND :et<=i.endTime) " + "OR(:st<=i.startTime AND :et>=i.endTime)";
		Query query = getSessionFactory().getCurrentSession().createQuery(HQL);
		query.setCalendar("st", startTime);
		query.setCalendar("et", endTime);

		return query.list();
	}

	/**
	 * 基于指定时间，查询指定类型的监考信息
	 * 
	 * @param startTime
	 * @param endTime
	 * @param typeId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<InvigilationInfo> listInviInfos(Calendar startTime, Calendar endTime, long typeId) {
		String HQL = "FROM InvigilationInfo i WHERE (:st>=i.startTime AND :st<=i.endTime) "
				+ "OR (:et>=i.startTime AND :et<=i.endTime) "
				+ "OR(:st<=i.startTime AND :et>=i.endTime) AND i.currentStatusType.id = :typeId";
		Query query = getSessionFactory().getCurrentSession().createQuery(HQL);
		query.setCalendar("st", startTime);
		query.setCalendar("et", endTime);
		query.setLong("typeId", typeId);
		return query.list();
	}

	/**
	 * 基于指定监考信息状态，分页查询
	 * 
	 * @param inviTypeId
	 * @param firstResult
	 * @param maxResults
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<InvigilationInfo> listInviInfos(long inviTypeId, int firstResult, int maxResults) {
		String HQL = "FROM InvigilationInfo i WHERE i.currentStatusType.id = :typeId ORDER BY startTime";
		Query query = getSessionFactory().getCurrentSession().createQuery(HQL);
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResults);
		query.setLong("typeId", inviTypeId);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<InvigilationInfo> list(int firstResult, int maxResults) {
		// TODO Auto-generated method stub
		return getSessionFactory().getCurrentSession().createCriteria(InvigilationInfo.class)
				.addOrder(org.hibernate.criterion.Order.asc("startTime")).setFirstResult(firstResult)
				.setMaxResults(maxResults).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<InvigilationInfo> list() {
		// TODO Auto-generated method stub
		return getSessionFactory().getCurrentSession().createCriteria(InvigilationInfo.class)
				.addOrder(org.hibernate.criterion.Order.asc("startTime")).list();
	}

}

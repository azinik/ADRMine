package rainbownlp.util;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import rainbownlp.machineLearning.MLExample;

public class HibernateUtil {
	//private static SessionFactory sessionFactory;
	private static ServiceRegistry serviceRegistry;
	// configures settings from hibernate.cfg.xml
	public static SessionFactory sessionFactory;      
	public static Session loaderSession;
	public static Session saverSession;
	public static boolean changeDB=true;
	public static String db;
	public static String user;
	public static String pass;
	
	static{
		
		sessionFactory = configureSessionFactory();
		loaderSession = sessionFactory.openSession();
		saverSession = sessionFactory.openSession();
	}
	static boolean inTransaction = false;
	
	
	private static SessionFactory configureSessionFactory() throws HibernateException {
		Configuration configuration = new Configuration();
	    configuration.configure();
	    serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();        
	    sessionFactory = configuration.buildSessionFactory(serviceRegistry);
	    
	    return sessionFactory;
	}

	public static void changeConfigurationDatabase(String databaseURL, String user, String pass){
	   	    
	    Configuration configuration = new Configuration();
		configuration.configure();
//		serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();   
//		configuration.setProperty("hibernate.connection.url", databaseURL);
//		configuration.setProperty("hibernate.connection.username", user);
//		configuration.setProperty("hibernate.connection.password", pass);
//	    configuration.configure();
		configuration.setProperty("connection.url", databaseURL);
		configuration.setProperty("hibernate.connection.url", databaseURL);
		configuration.setProperty("hibernate.connection.username", user);
		configuration.setProperty("hibernate.connection.password", pass);
		
		configuration.getProperty("hibernate.connection.url");
	    serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry(); 
		
	  
	    sessionFactory = configuration.buildSessionFactory(serviceRegistry);
	    loaderSession = sessionFactory.openSession();
		saverSession = sessionFactory.openSession();
		MLExample.hibernateSession=loaderSession;
	}
	/**
	 * save object status
	 */
	public static void save(Object _object, Session session) {
		try {
			session.beginTransaction();
			session.saveOrUpdate(_object);
			session.flush();
			session.getTransaction().commit();
		} catch (HibernateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void save(Object _object) {
		save(_object, loaderSession);
	}
	
	public static List<?> executeReader(String hql)
	{
		return executeReader(hql, null, null, loaderSession);
	}
	
	public static List<?> executeReader(String hql, HashMap<String, Object> params)
	{
		return executeReader(hql, params, null, loaderSession);
	}
	
	public static List<?> executeReader(String hql, HashMap<String, Object> params, Integer limit)
	{
		return executeReader(hql, params, limit, loaderSession);
	}
	public static List<?> executeReader(String hql, HashMap<String, Object> params, Integer limit, Session session)
	{
		Query q = session.createQuery(hql);
		
		if(params!=null)
			for(String key :params.keySet())
			{
				Object val = params.get(key);
				if(val instanceof String)
					q.setString(key, (String)val);
				else
					q.setInteger(key, (Integer)val);
			}
		if(limit!=null)
			q.setMaxResults(limit);
		
		List<?> result_list = 
			q.list();
		
		return result_list;
	}
	
	
	
	public static void executeNonReader(String hql, HashMap<String, Object> params)
	{
		if(!inTransaction)
		{
//			session = sessionFactory.openSession();
			saverSession = clearSession(saverSession);
			saverSession.beginTransaction();
		}
		
		Query qr = saverSession.createQuery(hql);
		if (params!=null)
		for(String key :params.keySet())
		{
			Object val = params.get(key);
			qr.setParameter(key, val);
		}
		
		qr.executeUpdate();
		
		if(!inTransaction)
		{
			saverSession.flush();
			saverSession.getTransaction().commit();
//			session.close();
			
		}
	}
	
	
	public static Object getHibernateTemplate() {
		// TODO Auto-generated method stub
		return null;
	}
	public static void executeNonReader(String hql) {
		executeNonReader(hql, null);
		
	}
	public static void startTransaction() {
		saverSession = sessionFactory.openSession();
		saverSession.beginTransaction();
		inTransaction = true;
	}
	
		  
		
	public static void endTransaction() {
		saverSession.flush();
		if(!saverSession.getTransaction().wasCommitted())
			saverSession.getTransaction().commit();
		saverSession.close();
		inTransaction = false;
	}
	public static void clearLoaderSession()
	{
		loaderSession = clearSession(loaderSession);
	}
	public static Session clearSession(Session session)
	{
		if(session!=null && session.isOpen()){
			session.clear();
			session.close();
		}
		session= sessionFactory.openSession();
		return session;
	}
	
	
	public static void flushTransaction() {
		saverSession.flush();
		saverSession.getTransaction().commit();
		
	}
	public static Object executeGetOneValue(String sql,
			HashMap<String, Object> params) {
		Session session = sessionFactory.openSession();
		Query query = session.createSQLQuery(sql);
		if (params!=null)
			for(String key :params.keySet())
			{
				Object val = params.get(key);
				query.setParameter(key, val);
			}
		Object oneVal = query.uniqueResult();
		session.clear();
		session.close();
		return oneVal;
	}
}

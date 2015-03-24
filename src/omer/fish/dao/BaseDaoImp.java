package omer.fish.dao;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * All DAO implementations should extend this class so they get the session injected. They could also use
 * getCurrentSession() method to get the session for this transaction.
 * 
 * @author Omer
 * 
 */
public abstract class BaseDaoImp implements BaseDao {

	/** Default Session Factory: For PCS DB */
	@Autowired
	private SessionFactory sessionFactory;

	/** Default DataSource: For PCS DB */
	@Autowired
	private DataSource dataSource;

	/** Simple JDBC Template for PCS DB */
	private SimpleJdbcTemplate jdbcTemplate;

	@Value("${hibernate.batch.size}")
	private String batchSize;

	public Session getCurrentSession() {
		// getCurrentSession() always returns the same session when called within the same transaction!!!
		return sessionFactory.getCurrentSession();
	}

	/**
	 * getDataSource
	 * 
	 * @return
	 */
	public DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * getJdbcTemplate
	 * 
	 * @return
	 */
	public synchronized SimpleJdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			this.jdbcTemplate = new SimpleJdbcTemplate(this.dataSource);
		}
		return jdbcTemplate;
	}

	/* Support basic CRUD operations for all DAO classes. */

	@Transactional
	public <T> T save(T object) {
		this.getCurrentSession().saveOrUpdate(object);
		return object;
	}

	@Transactional
	public <T> List<T> save(List<T> objects) {
		int count = 0;
		for (Object o: objects) {
			this.getCurrentSession().saveOrUpdate(o);
			if ( ++count % Integer.parseInt(this.batchSize) == 0 ) {
			    //flush a batch of updates and release memory:
				this.flush();				
			}
		}
		return objects;
	}

	@Transactional
	public <T> T merge(T object) {
		this.getCurrentSession().merge(object);
		return object;
	}

	@Transactional
	public <T> void delete(T object) {
		this.getCurrentSession().delete(object);
	}

	@Transactional
	public <T> void delete(long id) {
		//FIXME which model should be deleted with this id?
		this.getCurrentSession().delete(id);
	}

	@Transactional(readOnly = true)
	public <T> boolean exists(Class<T> c, long id) {
		return this.getCurrentSession().get(c, id) != null;
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public <T> T find(Class<T> c, long id) {
		return (T) this.getCurrentSession().get(c, id);
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public <T> List<T> findAll(Class<T> c) {
		List<T> newList = new ArrayList<T>();
		List list = this.getCurrentSession().createQuery("from " + c.getSimpleName()).list();
		for (Object o: list) {
			newList.add((T)o);
		}
		return newList;
	}

	/**
	 * Should be called for batch updates when batch number of save() were called.
	 */
	@Transactional
	public void flush() {
		this.getCurrentSession().flush();
		this.getCurrentSession().clear();
	}
}

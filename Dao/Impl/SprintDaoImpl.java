package com.pennant.prodmtr.Dao.Impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.pennant.prodmtr.Dao.Interface.SprintDao;
import com.pennant.prodmtr.model.Dto.ModuleDTO;
import com.pennant.prodmtr.model.Dto.UserDto;
import com.pennant.prodmtr.model.Entity.FunctionalUnit;
import com.pennant.prodmtr.model.Entity.Module;
import com.pennant.prodmtr.model.Entity.Sprint;
import com.pennant.prodmtr.model.Entity.SprintResource;
import com.pennant.prodmtr.model.Entity.SprintTasks;
import com.pennant.prodmtr.model.Entity.Task;
import com.pennant.prodmtr.model.Entity.User;

@Repository
@Transactional
public class SprintDaoImpl implements SprintDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public List<Sprint> getBaskLogs() {
		String query = "SELECT s FROM Sprint s WHERE EXISTS (SELECT 1 FROM Task t WHERE t.module.id = s.moduleId.id AND t.taskCompletedDateTime IS NULL)";
		return entityManager.createQuery(query, Sprint.class).getResultList();
	}

	@Override
	public Sprint getSprintDetails(int sprintId) {
		return entityManager.find(Sprint.class, sprintId);
	}

	@Override
	public List<Task> getTasks(int modlId) {
		String query = "SELECT t FROM Task t WHERE t.module.id = :modlId";

		return entityManager.createQuery(query, Task.class).setParameter("modlId", modlId).getResultList();
	}

	@Override
	public List<Sprint> getAllSprints() {
		String query = "SELECT s FROM Sprint s";
		return entityManager.createQuery(query, Sprint.class).getResultList();
	}

	// @Override
	// public List<SprintTasks> allTaskBySprintId(int sprintId) {
	// String query = "SELECT st FROM SprintTasks st WHERE st.id.sprnId = :sprintId";
	//
	// return entityManager.createQuery(query, SprintTasks.class).setParameter("sprintId", sprintId).getResultList();
	// }

	@Override
	public List<SprintTasks> getAllTasksBySprintId(Sprint sprintId) {
		String query = "SELECT st FROM SprintTasks st WHERE st.id.sprnId = :sprintId";
		return entityManager.createQuery(query, SprintTasks.class).setParameter("sprintId", sprintId).getResultList();
	}

	@Override
	public Sprint storeSprint(Sprint sprint) {

		if (sprint.getSprintId() == 0) {
			entityManager.persist(sprint); // New entity, use persist
		} else {
			entityManager.merge(sprint); // Existing entity, use merge
		}
		System.out.println("getting from database" + sprint);
		return sprint;
	}

	@Override
	public List<ModuleDTO> getSprintModulesByProjectId(int projectId) {
		short pid = (short) projectId;
		String query = "SELECT m FROM com.pennant.prodmtr.model.Entity.Module m WHERE m.moduleProject.projectId = :projectId AND m.moduleId NOT IN (     SELECT s.moduleId.id     FROM com.pennant.prodmtr.model.Entity.Sprint s )";
		TypedQuery<Module> typedQuery = entityManager.createQuery(query, Module.class);
		typedQuery.setParameter("projectId", pid);
		// sysout
		List<Module> moduleList = typedQuery.getResultList();

		for (Module m : moduleList) {
			System.out.println(m);
		}

		List<ModuleDTO> Mdto = new ArrayList<>();
		for (Module m : moduleList) {
			ModuleDTO md = ModuleDTO.fromEntity(m);
			Mdto.add(md);
		}

		System.out.println(moduleList.get(0) + "  divider  " + Mdto.get(0).getModl_id());
		return Mdto;
	}

	@Override
	public List<FunctionalUnit> getFunctionalUnitsByModId(int modlId, int prjid) {
		short mId = (short) modlId;
		short pId = (short) prjid;
		String funstatus = null;
		String query = "SELECT fu FROM FunctionalUnit fu WHERE fu.id.module.id = :modlId AND fu.projectId.projectId = :prjid AND fu.funStatus is null";

		return entityManager.createQuery(query, FunctionalUnit.class).setParameter("modlId", mId)
				.setParameter("prjid", pId).getResultList();
	}

	public Task storeTask(Task task) {
		if (task.getTaskId() == 0) {
			entityManager.persist(task); // New entity, use persist
		} else {
			entityManager.merge(task); // Existing entity, use merge
		}
		return task;
	}

	public List<UserDto> getAllResources() {
		String jpql = "SELECT r FROM User r";
		TypedQuery<User> query = entityManager.createQuery(jpql, User.class);
		List<User> users = query.getResultList();

		List<UserDto> userDtos = users.stream().map(UserDto::fromEntity).collect(Collectors.toList());

		return userDtos;
	}

	public void storeSprintResource(SprintResource src) {
		entityManager.persist(src);
	}

	public void storeSprintTasks(SprintTasks sprintTask) {
		// TODO Auto-generated method stub
		entityManager.persist(sprintTask);

	}

	@Override
	public List<Sprint> getSprintByProjId(int projId) {
		TypedQuery<Sprint> query = entityManager
				.createQuery("SELECT s FROM Sprint s where projectId.projectId = :projId", Sprint.class);
		query.setParameter("projId", (short) projId);
		List<Sprint> sprints = query.getResultList();
		return sprints;
	}

	public void updateFunctionalstatus(int funit) {
		String status = "Task";
		String qry = "UPDATE FunctionalUnit f SET f.funStatus = :status WHERE f.id.funitid = :funit";

		entityManager.createQuery(qry).setParameter("funit", funit).setParameter("status", status).executeUpdate();
	}

}
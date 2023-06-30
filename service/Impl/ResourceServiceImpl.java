
package com.pennant.prodmtr.service.Impl;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.pennant.prodmtr.Dao.Interface.ResourceDao;
import com.pennant.prodmtr.model.Dto.ResourceFilter;
import com.pennant.prodmtr.model.Dto.UserDto;
import com.pennant.prodmtr.model.Entity.User;
import com.pennant.prodmtr.model.Input.UserInput;
import com.pennant.prodmtr.service.Interface.ResourceService;

@Component
@Transactional
public class ResourceServiceImpl implements ResourceService {

	private final ResourceDao resourceDAO;
	private final Logger logger = LoggerFactory.getLogger(ResourceServiceImpl.class);

	@Autowired
	public ResourceServiceImpl(ResourceDao resourceDAO) {
		this.resourceDAO = resourceDAO;
	}

	/**
	 * Retrieves all resources
	 * 
	 * @return List of UserDto representing all resources
	 */
	@Override
	public List<UserDto> getAllResources() {
		logger.info("Entered the getAllResources in ResourceServiceImpl");

		try {
			logger.debug("Retrieving all resources from the DAO layer");
			List<UserDto> resources = resourceDAO.getAllResources();
			logger.debug("Retrieved {} resources", resources.size());
			return resources;
		} catch (Exception e) {
			logger.error("An error occurred while getting all resources", e);
			throw new RuntimeException("Failed to get all resources", e);
		}
	}

	/**
	 * Filters resources based on the provided criteria.
	 * 
	 * @param resourceFilter ResourceFilter object containing the filter criteria
	 * @return List of UserDto representing the filtered resources
	 */
	@Override
	public List<UserDto> filterResources(ResourceFilter resourceFilter) {
		Short roleFilter = resourceFilter.getRoleFilter();
		Short projectFilter = resourceFilter.getProjectFilter();

		try {
			logger.info("Filtering resources with roleFilter = {} and projectFilter = {}", roleFilter, projectFilter);
			List<UserDto> filteredResources = resourceDAO.filterResources(roleFilter, projectFilter);
			logger.debug("Filtered {} resources", filteredResources.size());
			return filteredResources;
		} catch (Exception e) {
			logger.error("An error occurred while filtering resources", e);
			throw new RuntimeException("Failed to filter resources", e);
		}
	}

	/**
	 * Retrieves a resource by display name.
	 * 
	 * @param displayName Display name of the resource
	 * @return User object representing the resource
	 */
	@Override
	public User getResourceByDisplayName(String displayName) {
		try {
			logger.info("Getting resource by display name: {}", displayName);
			User resource = resourceDAO.getResourceByDisplayName(displayName);
			if (resource != null) {
				logger.debug("Retrieved resource with display name: {}", displayName);
			} else {
				logger.debug("Resource with display name {} not found", displayName);
			}
			return resource;
		} catch (Exception e) {
			logger.error("An error occurred while getting resource by display name: {}", displayName, e);
			throw new RuntimeException("Failed to get resource by display name: " + displayName, e);
		}
	}

	/**
	 * Adds a new user.
	 * 
	 * @param userInput UserInput object containing the user information
	 */
	public void addUser(UserInput userInput) {
		try {
			logger.info("Adding a new user");
			User user = userInput.toEntity();
			// Add the user to the DAO layer for persistence
			resourceDAO.addUser(user);
			logger.debug("Added a new user: {}", user);
		} catch (Exception e) {
			logger.error("An error occurred while adding a new user", e);
			throw new RuntimeException("Failed to add a new user", e);
		}
	}

	/**
	 * Saves an existing resource.
	 * 
	 * @param existingResource UserInput object containing the existing resource information
	 */
	@Override
	public void save(UserInput existingResource) {
		try {
			existingResource.setUserCreationDate(existingResource.getUserCreationDate());
			existingResource.setUserLastUpdatedDate(new Date());

			User user = existingResource.toEntity();
			logger.info("Saving existing resource: {}", user);
			// Save the existing resource in the DAO layer
			resourceDAO.saveUser(user);
			logger.debug("Saved existing resource: {}", user);
		} catch (Exception e) {
			logger.error("An error occurred while saving existing resource", e);
			throw new RuntimeException("Failed to save existing resource", e);
		}
	}

	/**
	 * Retrieves all project managers.
	 * 
	 * @return List of User representing all project managers
	 */
	@Override
	public List<User> getAllProjManagers() {
		try {
			logger.info("Retrieving all project managers");
			List<User> projectManagers = resourceDAO.getAllProjManagers();
			logger.debug("Retrieved {} project managers", projectManagers.size());
			return projectManagers;
		} catch (Exception e) {
			logger.error("An error occurred while retrieving all project managers", e);
			throw new RuntimeException("Failed to retrieve all project managers", e);
		}
	}

	/**
	 * Retrieves users by project ID.
	 * 
	 * @param projectId ID of the project
	 * @return List of User representing the users in the project
	 */
	@Override
	public List<User> getUsersByProjectId(int projectId) {
		try {
			logger.info("Retrieving users by project ID: {}", projectId);
			List<User> users = resourceDAO.getUsersByProjectId(projectId);
			logger.debug("Retrieved {} users by project ID: {}", users.size(), projectId);
			return users;
		} catch (Exception e) {
			logger.error("An error occurred while retrieving users by project ID: {}", projectId, e);
			throw new RuntimeException("Failed to retrieve users by project ID: " + projectId, e);
		}
	}

	/**
	 * Retrieves a resource by ID.
	 * 
	 * @param userId ID of the resource
	 * @return User object representing the resource
	 */
	@Override
	public User getResourceById(int userId) {
		try {
			logger.info("Retrieving resource by ID: {}", userId);
			User resource = resourceDAO.getResourceById(userId);
			logger.debug("Retrieved resource by ID: {}", userId);
			return resource;
		} catch (Exception e) {
			logger.error("An error occurred while retrieving resource by ID: {}", userId, e);
			throw new RuntimeException("Failed to retrieve resource by ID: " + userId, e);
		}
	}

	/**
	 * Retrieves a resource by user ID.
	 * 
	 * @param userid User ID of the resource
	 * @return User object representing the resource
	 */
	@Override
	public User getResourceByUserId(int userid) {
		try {
			logger.info("Retrieving resource by user ID: {}", userid);
			User resource = resourceDAO.getResourceByUserid(userid);
			logger.debug("Retrieved resource by user ID: {}", userid);
			return resource;
		} catch (Exception e) {
			logger.error("An error occurred while retrieving resource by user ID: {}", userid, e);
			throw new RuntimeException("Failed to retrieve resource by user ID: " + userid, e);
		}
	}

	/**
	 * Saves a resource.
	 * 
	 * @param resource User object representing the resource to be saved
	 */
	@Override
	public void save(User resource) {
		try {
			logger.info("Saving resource: {}", resource);
			resourceDAO.saveUser(resource);
			logger.debug("Saved resource: {}", resource);
		} catch (Exception e) {
			logger.error("An error occurred while saving resource", e);
			throw new RuntimeException("Failed to save resource", e);
		}
	}

}

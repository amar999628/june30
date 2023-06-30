package com.pennant.prodmtr.controller;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.pennant.prodmtr.exceptions.ResourceNotFoundException;
import com.pennant.prodmtr.model.Dto.ProjectDto;
import com.pennant.prodmtr.model.Dto.ResTaskFilter;
import com.pennant.prodmtr.model.Dto.ResourceFilter;
import com.pennant.prodmtr.model.Dto.TaskDto;
import com.pennant.prodmtr.model.Dto.UserDto;
import com.pennant.prodmtr.model.Entity.Role;
import com.pennant.prodmtr.model.Entity.User;
import com.pennant.prodmtr.model.Input.UserInput;
import com.pennant.prodmtr.model.view.TaskCountview;
import com.pennant.prodmtr.service.Interface.ProjectService;
import com.pennant.prodmtr.service.Interface.ResourceService;
import com.pennant.prodmtr.service.Interface.RoleService;
import com.pennant.prodmtr.service.Interface.TaskService;

@Controller
public class ResourceController {
	private final ResourceService resourceService;
	private final ProjectService projectService;
	private final RoleService roleService;
	private final TaskService taskService;
	private final static Logger logger = LoggerFactory.getLogger(ResourceController.class);

	@Autowired
	public ResourceController(ResourceService resourceService, ProjectService projectService, RoleService roleService,
			TaskService taskService) {
		this.resourceService = resourceService;
		this.projectService = projectService;
		this.roleService = roleService;
		this.taskService = taskService;
	}

	@RequestMapping(value = "/resources", method = RequestMethod.GET)
	public String getAllResources(Model model) {
		try {
			// Log the information
			logger.info("Entered the getAllResources method");

			// Fetch all resources
			List<UserDto> resources = resourceService.getAllResources();
			logger.info("Fetched all resources. Total resources: {}", resources.size());

			List<ProjectDto> projects = projectService.getAllProjects();
			logger.info("Fetched all projects. Total projects: {}", projects.size());

			List<Role> roles = roleService.getAllRoles();
			logger.info("Fetched all roles. Total roles: {}", roles.size());

			// fetch all data about resources by calling the service and Dao layers about resources
			for (UserDto resource : resources) {
				int completedTasks = taskService.getCompletedTasksByUserId(resource.getUserId());
				int totalTasks = taskService.getTotalTasksByUserId(resource.getUserId());
				double performanceScore = taskService.calculatePerformanceScore(completedTasks, totalTasks);
				double hoursWorked = taskService.getHoursWorkedByUserId(resource.getUserId());

				resource.setPerformanceScore(performanceScore);
				resource.setHoursWorked(hoursWorked);
				resource.setTasksCompleted(completedTasks);

				// Log the calculated information for each resource
				logger.info(
						"Calculated performance score, hours worked, and tasks completed for resource with ID {}: performanceScore={}, hoursWorked={}, tasksCompleted={}",
						resource.getUserId(), performanceScore, hoursWorked, completedTasks);
			}

			// Add resources, projects, and roles to the model
			model.addAttribute("resources", resources);
			model.addAttribute("projects", projects);
			model.addAttribute("roles", roles);

			// Return the view name
			return "ResourceHome";
		} catch (ResourceNotFoundException e) {
			// Log the exception
			logger.error("Resource not found", e);

			// Handling the exception by customizing the error handling logic

			String errorMessage = "Resource not found: " + e.getMessage();
			model.addAttribute("errorMessage", errorMessage);

			// - Redirect to an error page with the error message
			return "errorPage";
		} catch (TaskServiceException e) {
			// Log the exception
			logger.error("An error occurred in the task service", e);

			// Handle the exception by customizing the error handling logic
			// For example:
			String errorMessage = "Error in task service: " + e.getMessage();
			model.addAttribute("errorMessage", errorMessage);

			// Customize the error handling logic based on your requirements
			return "errorPage";
		} catch (Exception e) {
			// Log the exception
			logger.error("An unexpected error occurred", e);

			// Handling other types of exceptions by customizing the error handling logic

			String errorMessage = "An unexpected error occurred: " + e.getMessage();
			model.addAttribute("errorMessage", errorMessage);

			// Customize the error handling logic based on your requirements
			return "errorPage";
		}
	}

	@RequestMapping(value = "/resources/filter", method = RequestMethod.GET)
	@ResponseBody
	public String getFilteredResources(@Validated ResourceFilter resourceFilter, BindingResult bindingResult) {
		try {
			// Log the information
			logger.info("Entered the getFilteredResources method");

			// Validate the resource filter
			if (bindingResult.hasErrors()) {
				// Log validation errors
				logger.error("Validation errors occurred for resource filter: {}", bindingResult.getAllErrors());

				// Return appropriate error response
				return "Validation Error";
			}

			// Retrieve filtered resources based on the filter criteria
			List<UserDto> filteredResources = resourceService.filterResources(resourceFilter);
			logger.info("Filtered resources. Total resources: {}", filteredResources.size());

			// Convert the filtered resources to JSON using Gson
			Gson gson = new Gson();
			String json = gson.toJson(filteredResources);

			// Return the JSON response
			return json;
		} catch (Exception e) {
			// Log the exception
			logger.error("An error occurred while filtering resources", e);

			// Returning appropriate error response
			return "Error occurred while filtering resources";
		}
	}

	@RequestMapping(value = "/resources/details", method = RequestMethod.GET)
	public String getResourceDetails(@RequestParam(name = "displayName") String displayName, Model model) {
		try {
			// Log the information
			logger.info("Entered the getResourceDetails method");

			// Print the display name
			logger.info("Display Name: {}", displayName);

			// Retrieve the resource by display name
			User resource = resourceService.getResourceByDisplayName(displayName);
			model.addAttribute("resource", resource);

			// Print the user's employee ID
			logger.info("User Employee ID: {}", resource.getUserEmployeeId());

			// Return the view name
			return "user_details";
		} catch (Exception e) {
			// Log the exception
			logger.error("An error occurred while fetching resource details", e);

			// Redirect to an error page
			return "errorPage";
		}
	}

	@RequestMapping(value = "/resources/update", method = RequestMethod.GET)
	public String updateResource(@RequestParam("userId") int userId, Model model) {
		try {
			// Log the information
			logger.info("Entered the updateResource method");

			// Retrieve the resource by user ID
			User resource = resourceService.getResourceByUserId(userId);
			List<Role> roles = roleService.getAllRoles();

			// Print the resource
			logger.info("Resource: {}", resource);

			// Add the resource and roles to the model
			model.addAttribute("resource", resource);
			model.addAttribute("roles", roles);

			// Return the view name
			return "update_resource";
		} catch (Exception e) {
			// Log the exception
			logger.error("An error occurred while updating resource", e);

			// Redirect to an error page
			return "errorPage";
		}
	}

	@RequestMapping(value = "/resources/updateSuccess", method = RequestMethod.POST)
	public String updateResourceSuccess(@Validated UserInput userInput) {
		try {
			// Log the information
			logger.info("Entered the updateResourceSuccess method");

			// Retrieve the existing resource from the database using the original user ID
			User resource = resourceService.getResourceByUserId(userInput.getUserId());

			// Create a new Role object and set the user's role ID
			Role role = new Role();
			role.setRoleId(userInput.getUserRole());

			// Update the resource with the new role and status
			resource.setUserRole(role);
			resource.setUserStatus(userInput.getUserStatus());

			// Save the updated resource
			resourceService.save(resource);

			// Redirect to the resources page
			return "redirect:/resources";
		} catch (Exception e) {
			// Log the exception
			logger.error("An error occurred while updating resource", e);

			// Redirect to an error page
			return "errorPage";
		}
	}

	@RequestMapping(value = "/resources/AddResource", method = RequestMethod.GET)
	public String addResource(Model model) {
		try {
			// Log the information
			logger.info("Entered the addResource method");

			// Add necessary logic
			List<Role> roles = roleService.getAllRoles();
			model.addAttribute("roles", roles);

			// Return the view name
			return "AddResource";
		} catch (Exception e) {
			// Log the exception
			logger.error("An error occurred while adding a resource", e);

			// Redirect to an error page
			return "errorPage";
		}
	}

	@RequestMapping(value = "/resources/addSuccess", method = RequestMethod.POST)
	public String addResourceSuccess(@Validated UserInput userInput, Model model) {
		try {
			// Log the information
			logger.info("Entered the addResource method");

			// Set the creation and last updated dates
			userInput.setUserCreationDate(new Date());
			userInput.setUserLastUpdatedDate(new Date());

			// Add the user resource
			resourceService.addUser(userInput);

			// Redirect to the resources page
			return "redirect:/resources";
		} catch (Exception e) {
			// Log the exception
			logger.error("An error occurred while adding a resource", e);

			// Redirect to an error page
			return "errorPage";
		}
	}

	@RequestMapping(value = "/resources/tasks", method = RequestMethod.GET)
	public String viewTasksForUser(@RequestParam("userId") int userId, Model model) {
		// Retrieve tasks by user ID from the service layer
		List<TaskDto> tasks = taskService.getTasksByUserId(userId);

		// Retrieve all projects from the service layer
		List<ProjectDto> projects = projectService.getAllProjects();

		// Get the project task count from the service layer
		List<TaskCountview> taskCountList = taskService.getProjectTaskCount(tasks);

		// Add the necessary attributes to the model
		model.addAttribute("userId", userId);
		model.addAttribute("tasks", tasks);
		model.addAttribute("projects", projects);
		model.addAttribute("taskCountList", taskCountList);

		// Return the view name
		return "TasksByName";
	}

	@RequestMapping(value = "resources/tasks/filter", method = RequestMethod.GET)
	@ResponseBody
	public String filterTasksByResources(@Validated ResTaskFilter resTaskFilter, BindingResult bindingResult) {
		try {
			// Log the information
			logger.info("/tasks/filter is called");
			logger.info("Status: {}", resTaskFilter.getStatus());
			logger.info("Project ID: {}", resTaskFilter.getProjectId());

			// Validate the task filter
			if (bindingResult.hasErrors()) {
				// Log validation errors
				logger.error("Validation errors occurred for task filter: {}", bindingResult.getAllErrors());

				// Return appropriate error response
				return "Validation Error";
			}

			// Retrieve filtered tasks based on the filter criteria
			List<TaskDto> tasks = taskService.filterTasks(resTaskFilter);

			// Convert the filtered tasks to JSON using Gson
			Gson gson = new Gson();
			String json = gson.toJson(tasks);

			// Return the JSON response
			return json;
		} catch (Exception e) {
			// Log the exception
			logger.error("An error occurred while filtering tasks", e);

			// Returning appropriate error response
			return "Error occurred while filtering tasks";
		}
	}

	@RequestMapping(value = "/user_details", method = RequestMethod.GET)
	public String getResourceDetailsById(@RequestParam(name = "userId") int userId, Model model) {
		try {
			// Log the information
			logger.info("getResourceDetailsById is called");
			logger.info("User ID: {}", userId);

			// Retrieve the resource details by user ID
			User resource = resourceService.getResourceById(userId);

			// Add the resource to the model
			model.addAttribute("resource", resource);

			// Return the view name
			return "user_details";
		} catch (Exception e) {
			// Log the exception
			logger.error("An error occurred while retrieving resource details", e);

			// Handle the exception accordingly.
			// For example, you can redirect to an error page or display a friendly message to the user.
			model.addAttribute("errorMessage", "An error occurred while retrieving resource details");
			return "errorPage";
		}
	}

}
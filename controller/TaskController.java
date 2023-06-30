package com.pennant.prodmtr.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.pennant.prodmtr.model.Dto.TFilterCriteria;
import com.pennant.prodmtr.model.Dto.TaskDto;
import com.pennant.prodmtr.model.Entity.Task;
import com.pennant.prodmtr.model.Entity.User;
import com.pennant.prodmtr.model.view.TaskUpdateFormModel;
import com.pennant.prodmtr.service.Interface.TaskService;

@Controller
public class TaskController {

	private final TaskService taskService;
	private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

	public TaskController(TaskService taskService) {
		this.taskService = taskService;
	}

	// method for view the tasks for user
	@RequestMapping(value = "/tasksbyid", method = RequestMethod.GET)
	public String viewTasksForUser(Model model, HttpSession session) {
		// Check if the user is logged in
		User user = (User) session.getAttribute("user");
		if (user == null) {
			// User is not logged in, redirect to the login page
			return "redirect:/login";
		}

		// User is logged in, fetch the tasks
		List<TaskDto> tasks = taskService.getTasksByUserId(user.getUserId());

		// Add the tasks to the model
		model.addAttribute("tasks", tasks);
		logger.info("tasks  loaded by user");

		// Return the view name
		String userTask = "User";
		model.addAttribute("Task", userTask);
		return "Taskslist";
	}

	@RequestMapping(value = "/tasks", method = RequestMethod.GET)
	public String viewAllTasks(Model model) {

		List<TaskDto> tasks = taskService.getAllTasks();

		// Add the tasks to the model
		model.addAttribute("tasks", tasks);

		String userTask = "Task";
		model.addAttribute("Task", userTask);
		// Return the view name
		return "Taskslist";
	}

	@RequestMapping(value = "/taskdetailsbyid", method = RequestMethod.GET)
	public String getAllTasks(Model model) {

		List<TaskDto> tasks = taskService.getAllTasks();

		model.addAttribute("tasks", tasks);

		return "tasksdetailsbyid";
	}

	@RequestMapping(value = "/updateTaskStatus", method = RequestMethod.POST)
	public String updateTaskStatus(@RequestParam("taskId") int taskId, Model model) {
		// Retrieve the existing task from the database using the task ID
		Task task = taskService.getTaskById(taskId);
		model.addAttribute("task", task);
		return "Taskslist";
	}

	@RequestMapping(value = "/updateSuccess", method = RequestMethod.POST)
	public String updateTaskStatusSuccess(@RequestParam("taskId") int taskId) {

		// Retrieve the existing task from the database using the task ID
		Boolean task = taskService.updateStatus(taskId);

		// Update the task status

		// Redirect to the task list page or show a success message
		return "redirect:/tasks";
	}

	@RequestMapping(value = "/Taskfilter", method = RequestMethod.POST)
	@ResponseBody
	public String filterTasks(@Validated TFilterCriteria filterCriteria) {
		List<TaskDto> filteredTasks = taskService.TfilterTasks(filterCriteria);
		System.out.println(filteredTasks);

		Gson gson = new Gson();
		String json = gson.toJson(filteredTasks);

		return json;
	}

	@RequestMapping(value = "/Indvtasks", method = RequestMethod.GET)
	public String viewIndvtasks(@RequestParam("projId") Integer projId, Model model) {
		List<Task> tasks = taskService.getTasksByProjectId(projId);
		model.addAttribute("tasks", tasks);
		return "Indvtasks";
	}

	@RequestMapping(value = "/setTaskStatus", method = RequestMethod.GET)
	public String setTaskStatus(@RequestParam int taskId, Model model, HttpSession session) {
		System.out.println("here in setTaskStatus");
		Task task = taskService.getTaskById(taskId);
		model.addAttribute("task", task);
		return "taskStatusUpdate";
	}

	@RequestMapping(value = "/setTaskDetails", method = RequestMethod.GET)
	public String setTaskUpdateFormModel(@Validated TaskUpdateFormModel taskUpdateFormModel, Model model) {
		System.out.println("here in setTaskStatus");
		taskService.updateTaskStatus(taskUpdateFormModel);
		return "redirect:activity";
	}

}

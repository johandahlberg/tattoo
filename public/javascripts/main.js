//Options for the spinning animation
var opts = {
	lines : 13, // The number of lines to draw
	length : 2, // The length of each line
	width : 2, // The line thickness
	radius : 5, // The radius of the inner circle
	corners : 1, // Corner roundness (0..1)
	rotate : 0, // The rotation offset
	direction : 1, // 1: clockwise, -1: counterclockwise
	color : '#000', // #rgb or #rrggbb or array of colors
	speed : 1, // Rounds per second
	trail : 60, // Afterglow percentage
	shadow : false, // Whether to render a shadow
	hwaccel : false, // Whether to use hardware acceleration
	className : 'spinner', // The CSS class to assign to the spinner
	zIndex : 2e9, // The z-index (defaults to 2000000000)
	top : 'auto', // Top position relative to parent in px
	left : 'auto' // Left position relative to parent in px
};

function labProjectDiv(parentdiv, name, status, samples, libraries) {
	
	var projectDiv = $('<div class="col-md-4 well"></div>')
	var div = $(parentdiv).append(projectDiv);
		
	$(projectDiv).append('<b> name: </b>' + name + '<br/>' + ' <b> status: </b> ' + status + '<br/>');	

	var ul1 = $('<ul>');
	
	$(samples).each(function(index, sample) {
		var item = $(document.createElement('li'));
		ul1.prepend(item.text(sample["name"]));
		
		$(sample["libraries"]).each(function(index, library) {
			var ul2 = $('<ul>');
			item.append(ul2);
			$(ul2).prepend($(document.createElement('li')).text(library["name"] + " - " + library["status"]));
		});
	});

	$(projectDiv).append(ul1);	
}

function projectDiv(name) {
	var div = '<div> <b>' + name + ' </b> </div>';
	return div;
}

function onGoingProjectDiv(name, status) {
	var div = '<div> ' + '<b>' + name + ' </b> <br/>' + 'pending: '
			+ status.pending + ' / ' + 'running: ' + status.running + ' / '
			+ 'finished: ' + status.finished + ' / ' + 'errored: '
			+ status.errored + '' + '</div>';
	return div;
}

function getPiperStatusOfProject(name, spinner) {
	$.getJSON("/project/analysis/" + name).done(function(data) {
		if (data == "unknown") {
			spinner.stop();
			$("#ongoing").append(onGoingProjectDiv(name, "unknown"));
		} else {
			spinner.stop();
			$("#ongoing").append(onGoingProjectDiv(name, data));
		}
	});
}

function getOngoing() {

	var spinner = new Spinner(opts).spin();
	$('#ongoing_spinner').append(spinner.el);

	$.getJSON("/project/ongoing").done(function(data) {
		$.each(data, function(k1, projects) {
			var name = projects["name"];
			getPiperStatusOfProject(name, spinner);
		});
	});
}

function labProject(project, targetDiv) {
	var name = project["name"];
	var status = project["status"];
	var samples = project["samples"];	
	labProjectDiv(targetDiv, name, status, samples);
}

function analysisProject(project, targetDiv) {
	var name = project["name"];
	targetDiv.append(projectDiv(name));
}

function addProjectsToDiv(jsonPath, targetDiv, projectTypeFunction,
		spinnerTarget) {

	var spinner = new Spinner(opts).spin();
	spinnerTarget.append(spinner.el);

	$.getJSON(jsonPath).done(function(data) {
		$.each(data, function(k1, project) {
			projectTypeFunction(project, targetDiv);
		});
	});

	spinner.stop();
}

function getAnalysisFinshed() {
	addProjectsToDiv("/project/analysisfinished", $('#analysis_finished'),
			analysisProject, $('#analysis_spinner'));
}

function getDelivered() {
	addProjectsToDiv("/project/delivered", $('#delivered'), analysisProject,
			$('#delivered_spinner'));
}

function getLabProjects() {
	addProjectsToDiv("/labproject", $('#lab_projects'), labProject,
			$('#lab_projects_spinner'));
}

function initialize() {
	getLabProjects();
	getOngoing();
	getAnalysisFinshed();
	getDelivered();
}

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

function onGoingProjectDiv(name, status) {
	var div = '<div> ' + '<b>' + name + ' </b> <br/>' + 'pending: '
			+ status.pending + ' / ' + 'running: ' + status.running + ' / '
			+ 'finished: ' + status.finished + ' / ' + 'errored: '
			+ status.errored + '' + '</div>';
	return div;
}

function getPiperStatusOfProject(name, spinner) {
	$.getJSON("/project/analysis/" + name).done(function(data) {
		console.log(data);
		if (data == "unknown") {
			console.log("Found unknown.")
			spinner.stop();
			$("#ongoing").append(onGoingProjectDiv(name, "unknown"));			
		} else {
			console.log("Found result.");
			spinner.stop();
			$("#ongoing").append(onGoingProjectDiv(name, data));			
		}
	});
}

function getOngoing() {

	var target;
	var spinner = new Spinner(opts).spin();
	target = $('#spinner').append(spinner.el);
	
	$.getJSON("/project/ongoing").done(function(data) {
		$.each(data, function(k1, projects) {
			var name = projects["name"];
			getPiperStatusOfProject(name, spinner);
		});
	});
}
 
var path = "../scripts/plotly.js//dist//plotly.js";
console.log(path);
require([path],
	function(Plotly){
		console.log(Plotly);
		//var data_source = 'https://raw.githubusercontent.com/plotly/datasets/master/wind_speed_laurel_nebraska.csv';
		var data_source = "../forex//out.csv";
		var x_name = "Time";
		//var y_name = "10 Min Sampled Avg";
		var y_name = "Avg";
		//var error_bar_name = "10 Min Std Dev";
		var error_bar_name = "Diff";
		Plotly.d3.csv(data_source, function(rows){
		var trace = {
		  type: 'scatter',                    // set the chart type
		  mode: 'lines',                      // connect points with lines
		  x: rows.map(function(row){          // set the x-data
			return row[x_name];
		  }),
		  y: rows.map(function(row){          // set the x-data
			return row[y_name];
		  }),
		  line: {                             // set the width of the line.
			width: 1
		  },
		  error_y: {
			array: rows.map(function(row){    // set the height of the error bars
			  return row[error_bar_name];
			}),
			thickness: 0.5,                   // set the thickness of the error bars
			width: 0
		  }
		};

		var layout = {
		  yaxis: {title: "Ratio"},       // set the y axis title
		  xaxis: {
			showgrid: false,                  // remove the x-axis grid lines
			tickformat: "%d %B %Y %H:%M:%S"              // customize the date format 
		  },
		  /*margin: {                           // update the left, bottom, right, top margin
			l: 40, b: 10, r: 10, t: 20
		  }*/
		};

		Plotly.plot(document.getElementById('wind-speed'), [trace], layout, {showLink: false});
	}
	)});
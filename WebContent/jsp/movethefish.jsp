<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<%@ page import="java.util.*" %>
<%@ page import="omer.fish.model.data.AnimatedEntity" %>
<%@ page import="omer.fish.model.FishManager" %>
<%@ page import="org.springframework.web.context.WebApplicationContext" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Move The Fish</title>
<link rel="stylesheet" href="css/styles.css">
<%
	/* retrieve the application context (not the servlet context) where the beans are. Note that getWebApplicationContext()
	can only retrieve the non-servlet context. */
	ServletContext servletContext = config.getServletContext();
	WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(servletContext);
	/* create the styles for each fish, by id, and apply their size and locations */
	FishManager fishManager = (FishManager)context.getBean("fishManager");
	List<AnimatedEntity> fishes = fishManager.getAllFish();
	out.print("<style>");
	for (AnimatedEntity fish: fishes) {
		out.print("#" + fish.getUniqueIdName() + "\n{ \nheight: " + fish.getH() + "px; width: " + fish.getW() + "px; position: absolute; left: " + 
		fish.getX() + "px; top: " + fish.getY() + "px" + "\n}\n");
	}
	out.print("</style>");
%>
<script type="text/javascript" src="scripts/jquery-1.9.0.js"></script>
<script type="text/javascript" src="scripts/ui/jquery-ui.js"></script>
<script type="text/javascript">

// the document ready handler. called only after the whole dom is loaded. put all functions and even handlers here, but 
// could have several ready methods and they would be called in order. Also, by writing all methods in the ready
// handler, we avoid polluting the global namespace.
$(function() {
	
	// INITIALIZATIONS
	//=================================================================================================================
	$.ajaxSetup({
		// setting requests defaults to json 
		dataType: 'json',
		contentType: 'application/json; charset=utf-8'		
	});
	// disable selection otherwise, when clicking in the window images get highlighted w/blue background
	$('#maindiv').css("user-select", "none");
	// initially, hide palette
	$('div.palette_body').toggle();

	// EVENT HANDLERS
	//=================================================================================================================
	// toggle collapsible module on mouse click
	$('div.caption img').bind('click', (function() {
		$('div.palette_body').toggle(500, function () {
			if ($(this).is(':hidden')) {
				$('#maindiv').animate({left: 40}, 500);
			}
			else {
				$('#maindiv').animate({left: 340}, 500);
			}
		});
	}));
	// select a fish from the palette
	$('div.palette_body').bind('click', (function(event) {
    	var selected_palette_fish = $(this).attr('selected_palette_fish');
		if (event.target.nodeName == "IMG") {
	    	if (selected_palette_fish) {
	    		$('#' + selected_palette_fish).parent().css({backgroundColor: '#bbbb08'});
	    	}
			$('#' + event.target.id).parent().css({backgroundColor: 'yellow'});
			$(this).attr('selected_palette_fish', event.target.id);
		}
		else {
	    	if (selected_palette_fish) {
				$('#' + selected_palette_fish).parent().css({backgroundColor: '#bbbb08'});
	    	}
	    	$(this).attr('selected_palette_fish', null);
		}
	}));
	// create or delete a fish
	$('#maindiv').bind('dblclick', function(event) {
		// NOTE: a dblclick event first generates also two mouse down and up events
		// if on an existing fish, tell server to delete that fish. if on an open space, ask server to create new fish selected from palette
		if (event.target.nodeName == "IMG") {
			// delete
	    	var fish$ = $('#' + event.target.id);
	    	fish$.attr('self_remove', 'true');
	    	delete_fish(fish$);
		}
		else if ($('div.palette_body').attr('selected_palette_fish')) {
			// create new
			console.log(event.pageX + ", " + event.pageY); // coords are absolute, so (340,0) if clicked on left-upper corner
			$.ajax({
				url: '/fish/post/createfish', 
				data: "{\"x\": " + event.pageX + ", \"y\": " + event.pageY + ", \"subType\": " + "\"" + $('div.palette_body').attr('selected_palette_fish') + "\"}",
				type: 'POST',
			    success: adjust_position_and_create_fish,
			});
		}
	});
	// if clicked on a fish, stop that fish animation and start drag and drop
	$('#maindiv').bind('mousedown', function(event) {
		// use the main div to store the fish id selected by user. 
		if (event.target.nodeName == "IMG") {
			var uniqueIdName =  event.target.id;
			fish$ = $("#" + uniqueIdName);
			$('#maindiv').attr('selected_fish', uniqueIdName);
			// if fish is moving, mark it as interrupted so that once user lets the fish go, animations don't keep moving it to
			// what is now an old target location
			if ($("#" + uniqueIdName).attr('is_moving')) {
				fish$.attr('move_interrupted', 'true');
			}
			// disable built-in drag and drop. we implement a manual one
			return false;
		}
	});
	// finish drag and drop
	$(document).bind('mouseup', function(event) {
		// move the selected fish upon mouse up 
		// TODO: no hardcoding!
		var tankWidth = 1300;
		var tankHeight = 700;
		// reconstruct the fish object from id stored on div and move it to new location
		if ($('#maindiv').attr('selected_fish')) {
			var selected_fish_id = $('#maindiv').attr('selected_fish');
			var selected_fish$ = $('#' + selected_fish_id);
			var fishX = event.pageX - selected_fish$.width() / 2 - $('#maindiv').offset().left;
			var fishY = event.pageY - selected_fish$.height() / 2;
			// make sure fish isn't dropped outside of screen. Also, note the adjustment for the horizontal displacement caused by the palette
			if ((event.pageX + selected_fish$.width() - $('#maindiv').offset().left) > tankWidth) 
				fishX = tankWidth - selected_fish$.width() - selected_fish$.width();
			if ((event.pageX - $('#maindiv').offset().left) < selected_fish$.width() / 2) fishX = 0;
			if ((event.pageY + selected_fish$.height()) > tankHeight) fishY = tankHeight - selected_fish$.height();
			// reset the target values
			selected_fish$.attr("target_x", fishX);
			selected_fish$.attr("target_y", fishY);
			// no need to set location on fish. it is already set when moving. Just report to server location of the moved fish
			var id = selected_fish_id.substring(selected_fish_id.lastIndexOf("_") + 1, selected_fish_id.length);
			var subType = event.target.id.substring(0, event.target.id.lastIndexOf("_"));
			$.ajax({
				url: '/fish/post/fishLocation', 
				data: "{\"id\": " + id + ", \"subType\": " + "\"" + subType + "\", \"x\": " + fishX + ", \"y\": " + fishY + "}",
				type: 'POST',
			});
			// reset flags
			$('#maindiv').attr('selected_fish', null);
			fish$.attr('is_moving', null);
			fish$.attr('move_interrupted', null);
		}
	});
	// move the selected fish upon mouse move as part of drag and drop
	$('#maindiv').bind('mousemove', function(event) {
		// reconstruct the fish object from id stored on div and move it to new location
		if ($('#maindiv').attr('selected_fish')) {
			var selected_fish$ = $('#' + $('#maindiv').attr('selected_fish'));
			// see if belonging to a school
			var parent$ = selected_fish$.parent();
			var fishX, fishY;
			if (!isSchoolFish(selected_fish$)) {
				fishX = event.pageX - $('#maindiv').offset().left - selected_fish$.width() / 2;
				// note the adjustment for the horizontal displacement caused by the palette
				if (fishX < 0) fishX = 0;
				fishY = event.pageY - selected_fish$.height() / 2;
			}
			else {
				fishX = event.pageX - parent$.offset().left - selected_fish$.width() / 2;
				// note the adjustment for the horizontal displacement caused by the palette
				fishY = event.pageY - parent$.offset().top - selected_fish$.height() / 2;
			}
			selected_fish$.css({
				// note the adjustment for the horizontal displacement caused by the palette
				left: fishX,
				top: fishY
			});
			if (selected_fish$.attr('is_moving')) 
				selected_fish$.attr('move_interrupted', 'true');
		}
	});
	
	// CORE FUNCTIONS
	//=================================================================================================================

	// delete a fish that was double-clicked or was deleted by another user
	function delete_fish(fish$) {
		var fish_full_id = fish$.attr('id');
		var id = fish_full_id.substring(fish_full_id.lastIndexOf("_") + 1, fish_full_id.length);
		var subType = fish_full_id.substring(0, fish_full_id.lastIndexOf("_"));
		var oldYStr = fish$.css("top");
		var oldY = Number(oldYStr.substring(0, oldYStr.length - 2));
		// delete
		$.ajax({
			url: '/fish/post/deletefish', 
			data: "{\"id\": " + id + ", \"subType\": " + "\"" + subType + "\"}",
			dataType: 'html', // reset from json b/c the response is not json, as the api returns nothing...
			type: 'POST',
			success: function() {
		    	// remove fish from this client. push it up
		    	// TODO: too many hard-coded values
		    	fish$.animate({top: oldY - 80}, 500);
		    	// bring it down
		    	fish$.animate({top: oldY}, 
		    			{
		    				duration: 500,
		    				complete: function() {
		    			    	// flip it. get old x scale. don't want to filp it horizontally, only vertically
		    			    	var scale_x = getScale_x($(this));
		    			    	if (matrix == 'none') {
		    			    		scale_x = 1;
		    			    	}
		    			    	else {
		    			    		scale_x =  Number(matrix.substr(7, matrix.length - 8).split(', ')[0]);
		    			    	}
		    			    	var scale = 'scale(' + scale_x + ',-1)';
		    			    	$(this).css('transform', scale);
		    				}
		    			}
		    	);
		    	fish$.animate({top: oldY - 50}, 300);
		    	// bring it down
		    	fish$.animate({top: oldY}, 300);
		    	// up again
		    	fish$.animate({top: oldY - 20}, {duration: 150, easing: "swing"});
		    	// bring it down
		    	fish$.animate({top: oldY}, {duration: 150, easing: "swing"});
		    	// make it shrink and delete it
		    	fish$.animate({width: fish$.width() / 4, height: fish$.height() / 4, opacity: 0}, 
		    			{
		    				duration: 2000,
		    				easing: "swing",
		    				complete: function() {
		    			    	$(this).remove();
		    				}
		    			}
		    	);
		    },
		});
	}
	// adjust school fish postion to the parent div position and only then create it
	function adjust_position_and_create_fish(fish) {
		// if a shcool fish, adjust to position relative to its shcool's div
		if (fish.type == 'SCHOOL_FISH') {
			schoolDiv$ = $('div[id*=' + fish.subType + ']');
			// offset() returns a position relative to the doc origin. 
			// if we don't adjust the coords will be relative to the school fish div. we need to delete the offset of the current
			// school div position from the fishx/y values
			console.log(schoolDiv$.offset().left)
			console.log(schoolDiv$.offset().top)
			
			// if palette_body is visible compensate for its width. otherwise, only compensate for the palette width (the narrow black part)
			fish.x = fish.x - schoolDiv$.offset().left + $('div.palette').width();
			if (!$('div.palette_body').is(':hidden')) {
				// palette_body is visible
				fish.x = fish.x + $('div.palette_body').width();
			}
			fish.y = fish.y - schoolDiv$.offset().top;
			console.log("after adjustment, fish.x, fish.y: " + fish.x + ", " + fish.y);
		}
		create_fish(fish);
	}
	// create a fish
	function create_fish(fish) {
		// create a fish out of an array. first, determine which div it's going into. 
		var divId = "#maindiv";
		if (fish.type == 'SCHOOL_FISH') {
			// it's a school fish. it goes under the (currently one only) school
			divId = '#' + $('div[id*=' + fish.subType + ']').attr('id');
		}
    	// display new fish on this client. First create it (invisible), then add the style to it. finally, animate to make visible
    	$('<img>').attr({id: fish.uniqueIdName, src: fish.path + "/" + fish.subType + "." + fish.extension}).
			css({top: fish.y, left: fish.x - $('#maindiv').offset().left, height: fish.h / 4, width: fish.w / 4, 
				position: "absolute", opacity: 0}).appendTo(divId).
			animate({width: fish.w, height: fish.h, opacity: 1}, {duration: 2000, easing: "swing"});
		$("#" + fish.uniqueIdName).attr("target_x", fish.x);
		$("#" + fish.uniqueIdName).attr("target_y", fish.y);
    }
	// check for updates in fish location. if the location has changed, move the fish to its new location.
	// To avoid a failed request stopping updates, use the success and complete callbacks.
	// Note that updateFishLocation gets executed first time upon dom load
	$(function updateFishLocation() {
	    // set timer to 1000 (TODO:should take from config file)
		$.ajax({
		    url: '/fish/get/allFishLocation', 
		    success: function(fishes) {
	    		// delete all fish that were deleted by others. look at this page's fish elements and see which of them does not exist
	    		// in the list returned by allFishLocation call. remove those.
	    		var deletedFish$ = $('#maindiv').find('*').not(function() { 		// not() returns a jquery matched set
	    			if ($(this).attr('self_remove')) {
	    				// let the remove animation delete it
	    				return true;
	    			}
	    			var id = $(this).attr("id");
	    			var fishNotFoundArray = $.grep(fishes, function(onefish) {		// grep returns an array. we need to check if it's empty
	    				return onefish.uniqueIdName == id;
	    			});
	    			return fishNotFoundArray.length > 0;
		  		});
	    		if (deletedFish$.size() > 0) {
	    			deletedFish$.each(function() {
	    				delete_fish($(this));
	    			});
	    		}
	    		// create new fish created by others. do the reverse of the delete. See which of the items returned by allFishLocation
	    		// don't exist in this page's list and add those as new elements to the dom
	    		var created = $.grep(fishes, function(onefish) { 					// grep() searches in array. will send one item (onefish) at a time into the callback
	    			var allFishElements = $('#maindiv').find('*').get(); 			// get() gets the actual dom elements as an array
	    			for (var i = 0; i < allFishElements.length; i++) {
	    				if (allFishElements[i].id == onefish.uniqueIdName) return false; 	// note the use of "id", not .attr('id')
	    			}
	    			return true;
	    		});
	    		if (created.length > 0) {
	    			for (var i = 0; i < created.length; i++) {
	    				create_fish(created[i]);
	    			};
	    		}
	    		// now, call the move function
		    	for (var i = 0; i < fishes.length; i++) {
		    		// target position is where the fish is going to. it may or may equal the current fish position, as it may be moving towards it.
		    		// if it equals or is undefined b/c it's the first time the page is loaded or the fish is new, let the fish move on...
		    		var fish$ = $('#' + fishes[i].uniqueIdName);
		    		var target_x = Number(fish$.attr("target_x"));
		    		var target_y = Number(fish$.attr("target_y"));
		    		if (isNaN(target_x) || !fish$.attr('is_moving') && (fishes[i].x != target_x || fishes[i].y != target_y)) {
		    			// if the fish is new or was interrupted it's targets are null. otherwise, in order to move it we it needs to have reached its
		    			// previous targets and new left/top values should be retrieved from the server
		    			// mark the fish is moving so that it can be interrupted, if needed
		    	   		fish$.attr('is_moving', 'true');
		    			// only move the fish if its target position has changed by the server or other users and it's already at its
		    			// previous target position. record the new target position.
		    			fish$.attr("target_x", fishes[i].x);
		    			fish$.attr("target_y", fishes[i].y);
			    		moveToNewLocation(fishes[i].uniqueIdName, fishes[i].moveDuration, fishes[i].turnDuration, fishes[i].turnPixLength, 
			    				fishes[i].turnWFactor, fishes[i].moveDelay, fishes[i].x, fishes[i].y,  fishes[i].w, fishes[i].h);
			    		// when all animation on fish is done, mark it as not moving
						fish$.promise().done(function(){
			    	   		fish$.attr('is_moving', null);
						});			
		    		}
		    	}
		    },
		    complete: function() {
		        // Schedule the next request when the current one's complete, whether successful or not
		        setTimeout(updateFishLocation, 1000);
		    }
	  	});
	});
	// move any fish/school to its new location. note that for new school fish this method will be called only once, after creation,
	// as the server moves only the school and not individual school fish. upon instantiation of a school fish and also if a user moves it
	// this method (at bottom) attempts to send the fish to its location in the school relative to where the user created it or
	// moved it to.
	// TODO: add code for school fish facing to the right. when just created, they need to be turned and moved as we do for left-facing school fish.
	function moveToNewLocation(uniqueIdName, moveDuration, turnDuration, turnPixLength, turnWFactor, moveDelay, x, y, w, h) {
		// TODO: no hardcoding!
		var tankWidth = 1300;
		var fish$ = $("#" + uniqueIdName);
		var oldX = Math.round(fish$.position().left);
		var oldY = Math.round(fish$.position().top);
		// if hasn't moved/new, nothing to do.
		if (oldY == y && oldX == x) {
			fish$.css({top: y, left: x, height: h, width: w});
			fish$.attr('is_moving', null);
			return;
		}
		// flip the fish towards the direction into which it is going. 
		// TODO: remove the limitation on the fish image name to indicate direction, if possible...
		var last_index = uniqueIdName.lastIndexOf("_");
		// if a school, move one _ further
		var suffix = uniqueIdName.substr((last_index - 7) >= 0 ? last_index - 7 : 0, 7);
		var is_school = false;
		var turnX = oldX;
		var fish_width = 0;
    	var scale_x = 1;
		if (suffix == '_school') {
			is_school = true;
			last_index = uniqueIdName.substr(0, last_index - 1).lastIndexOf("_");
			// if it's a school, take the transform value off the first child. all fish should have the same transform value
			var num_fish_in_school = fish$.children().size();
			if (num_fish_in_school > 0)
				scale_x = getScale_x(fish$.children().first());
			// set turnX to 0. It will be modified when turning each school fish, by adding each school fish's own oldX (see turn())
			turnX = 0;
			// make fish width that of the first child
			if (fish$.children().size() > 0)
				fish_width = fish$.children().first().width();
		}
		else {
			// get matrix off the fish
			scale_x = getScale_x(fish$);
			fish_width = fish$.width();
		}
    	var turn_width_displacement = (turnWFactor - 1) * fish_width / (turnWFactor * 2);
		minTurnW = fish_width / turnWFactor;
		if (x > oldX) {
			// try to create the illusion of turning to the right.
			// note the compensation in 'left' b/c when we adjust the width, the center of the fish shifts towards 'left' and causes
			// the viewer to think that the fish is moving to the left, so we have to compensate and move its center towards it's original position.
			// TODO: could improve on this by making the turn more appropriate for the fish size. small fish can turn on a dime, big ones, 
			// especially sharks, do a complete circle... Also, could adjust the width acoording to the fish type. A shark is quite wide,
			// so it should only shrink so much, while thin fish like trout (?) are thinner and when turning should be viewed as such...
			// note that a fish may or may not be turning...
			turnX += (turn_width_displacement - turnPixLength);
			// if (turnX + fish$.width() / 2 > tankWidth) turnX = tankWidth - fish$.width() / 2;
			if (uniqueIdName.substring(last_index - 2, last_index) == "_l" && scale_x == 1) {
				turn(is_school, fish$, moveDuration, moveDelay, oldX, oldY, x, y, w, turnDuration, turnX, minTurnW, -1, 1);
				// however, if it's a new school fish, move it to the school and check if need to turn it after joining the school.
				// do all ops sequentially!
				if (isSchoolFish(fish$)) {
					fish$.promise().done(function(){
					    // will be called when all the animations on the queue finish
						move(fish$, x, y, moveDuration);
						fish$.promise().done(function(){
							first_child$ = fish$.parent().children().first();
							// if it's the only child, no need to worry about direction
							if (first_child$ && first_child$ !== fish$) {
						    	var school_scale_x = getScale_x(first_child$);
								if (school_scale_x != -1) { // we know that this school fish is in -1 scale!
									turn(false, fish$, moveDuration, moveDelay, x, y, x, y, w, turnDuration, x + turnPixLength + turn_width_displacement, minTurnW, 1, 1);
								}
							}
						});			
					});			
				}
			}
			else if (uniqueIdName.substring(last_index - 2, last_index) == "_r" &&  scale_x == -1) {
				turn(is_school, fish$, moveDuration, moveDelay, oldX, oldY, x, y, w, turnDuration, turnX, minTurnW, 1, 1);
			}
			else {
				// no turning. Just move
				fish$.children().promise().done(function(){
				    // will be called when all the animations on the queue finish
					move(fish$, x, y, moveDuration);
				});			
			}
		}
		else if (x < oldX) {
			turnX += (turnPixLength + turn_width_displacement);
			if (uniqueIdName.substring(last_index - 2, last_index) == "_r" && scale_x == 1) {
				turn(is_school, fish$, moveDuration, moveDelay, oldX, oldY, x, y, w, turnDuration, turnX, minTurnW, -1, 1);
			}
			else if (uniqueIdName.substring(last_index - 2, last_index) == "_l" && scale_x == -1) {
				turn(is_school, fish$, moveDuration, moveDelay, oldX, oldY, x, y, w, turnDuration, turnX, minTurnW, 1, 1);
			}
			else {
				// no turning. Just move
				fish$.children().promise().done(function(){
				    // will be called when all the animations on the queue finish
					move(fish$, x, y, moveDuration);
				});			
				// however, if it's a new school fish check if need to turn it after joining the school
				if (isSchoolFish(fish$)) {
					first_child$ = fish$.parent().children().first();
					// if it's the only child, no need to worry about direction
					if (first_child$ && first_child$ !== fish$) {
				    	var school_scale_x = getScale_x(first_child$);
						if (school_scale_x != scale_x) {
							turn(false, fish$, moveDuration, moveDelay, x, y, x, y, w, turnDuration, x - turnPixLength - turn_width_displacement, minTurnW, -1, 1);
						}
					}
				}
			}
		}
	}
	// move a fish
	function move(fish$, x, y, moveDuration) {
		fish$.animate({left: x, top: y}, {
			duration: moveDuration,
			complete: function() {
				// a roundup error in jquery could cause the fish left/top to be off by a fraction due to floating number calculation. so
				// manually set the values to the expected move values
				fish$.css({top: y, left: x});
				// last thing, reset flags
				fish$.attr('is_moving', null);
				fish$.attr('move_interrupted', null);
		   	},
			step: function(step, effects) {
				if (($('#maindiv').attr('selected_fish') || $('#maindiv').attr('selected_fish') == $(this).attr('id')) &&
						fish$.attr('move_interrupted')) {	
					$(this).stop();
					fish$.attr('is_moving', null);
				}
			}
		});
	}
	// turn a fish in 4 steps. if it's a school of fish, however, call turn recursively for each shcool fish
	function turn(is_school, fish$, moveDuration, moveDelay, oldX, oldY, x, y, w, turnDuration, turnX, minTurnW, scaleX, scaleY) {
		console.log('this: ' + fish$.attr('id'));
		if (is_school) {
			// for each of the children call turn()
			fish$.children().each(function() {
				// add current fish location to turnX. Then turn
				turn(false, $(this), moveDuration, moveDelay, Math.round($(this).position().left), 0, 0, 0, 
						$(this).width(), turnDuration, turnX + $(this).position().left, /*$(this).width() */ /*// 4*/ minTurnW, scaleX, scaleY);
			});
			// then move the school. but wait till none of the shcool fish is being animated, that is, they are done turning
			fish$.children().promise().done(function(){
			    // will be called when all the animations on the queue finish
				move(fish$, x, y, moveDuration);
			});			
			return;
		}
		// NOTE: if I don't specify something in the first arg (properties) the animation seems to kick
		// out of order. this appears like a jquery bug! opacity: 1 has no effect.
		fish$.animate({opacity: 1}, {
			duration: moveDelay,
			complete: function() {
		    	fish$.animate({width: minTurnW, left: turnX}, 
		    			// first, move the fish forward in the same direction it was going but slowly make it thiner so it looks like it's truning
		    			{
		    				duration: turnDuration / 2,
		    				step: function() {
		    					if ($(this).attr('move_interrupted')) {
		    						// stop turning
		    						$(this).stop();
		    						// return to normal width. don't care if it happens while it starts moving again, so reset the is_moving flag right away
		    						$(this).animate({width: w}, turnDuration / 2);
		    						fish$.attr('is_moving', null);
		    					}
		    				},
		    				complete: function() {
		    					// second, flip the fish
		    					fish$.css('transform', 'scale(' + scaleX + ',' + scaleY + ')');
		    				}
		    			}
		    	).animate({width: w, left: oldX}, 
		    			// third, make the fish come back to it original position, but now it's facing the other way
		    			{
		    				duration: turnDuration / 2,
		    				step: function() {
		    					if ($(this).attr('move_interrupted')) {
		    						$(this).stop();
		    						$(this).animate({width: w}, turnDuration / 2);
		    						fish$.attr('is_moving', null);
		    					}
		    				},
		    				complete: function() {
		    					// fourth, finished turning, so now move to final destination (don't move school fish)
								if (!isSchoolFish(fish$)) {
		    						move(fish$, x, y, moveDuration);
		    					}
		    				}
		    			}
		    	);
			}
		});
	}
	
	// UTILITY FUNCTIONS
	//=================================================================================================================
	function isSchoolFish(fish$) {
		// a school fish is NOT directly under the main div, but rather under a school div. 
		// TODO: could have improved on this check to verify that the parent is indeed a school
		if (fish$.parent().attr('id') != 'maindiv') {
			return true;
		}
		return false;
	}
	// get the scale saved on the fish. if not exists, returns 1
	function getScale_x(fish$) {
    	matrix = fish$.css("transform");
    	var scale_x = 1;
    	if (matrix == 'none') {
    		scale_x = 1;
    	}
    	else {
    		scale_x =  Number(matrix.substr(7, matrix.length - 8).split(', ')[0]);
    	}
    	return scale_x;
	}

	
});
</script>
</head>
<body>
	<div class="palette">
		<div class="caption">
			<img src="images/rollup.gif" alt="rollup" title="rolls the palette"/>
		</div>
		<div class="palette_body">
			<div class="palette_body_text">Select a new fish from the palette and </div>
			<div class="palette_body_text">double-click anywhere in the aquarium</div>
			<div class="fish_palette_bg_div">
				<img class="fish_palette_img" id="shark_l" alt="" src="images/shark_l.gif">
			</div>
			<div class="fish_palette_bg_div">
				<img class="fish_palette_img" id="discus_l" alt="" src="images/discus_l.gif">
			</div>
			<div class="fish_palette_bg_div">
				<img class="fish_palette_img" id="bass_r" alt="" src="images/bass_r.gif">
			</div>
			<div class="fish_palette_bg_div">
				<img class="fish_palette_img" id="green_l" alt="" src="images/green_l.gif">
			</div>
			<div class="fish_palette_bg_div">
				<img class="fish_palette_img" id="trout_l" alt="" src="images/trout_l.gif">
			</div>
		</div>
	</div>
	<div id="maindiv">
		<c:forEach items="${fishes}" var="fish">
			<c:if test="${fish.type == 'FISH'}">
				<img id="${fish.uniqueIdName}" alt="" src="${fish.path}/${fish.subType}.${fish.extension}">
			</c:if>
		</c:forEach>
		<!-- TODO: make school div creation dynamic. Code should detect how many schools there are and create a div per
		school. Then it should populate it with using the subType -->
		<c:forEach items="${fishes}" var="fish">
			<c:if test="${fish.type == 'SCHOOL'}">
				<div id="${fish.subType}_${fish.id}">
					<c:forEach items="${fishes}" var="fish">
						<!-- TODO: when there are more schools, need to define a var given the above school 'subType' that will obviate
						the need for use of specific type as here: && fish.subType == 'discus_l' and add this to the 'if' statement below.
						for now, just assume all school fish are discus_l -->
						<c:if test="${fish.type == 'SCHOOL_FISH'}">
							<img id="${fish.uniqueIdName}" alt="" src="${fish.path}/${fish.subType}.${fish.extension}">
				  		</c:if>
					</c:forEach>
				</div>
			</c:if>
		</c:forEach>
	</div>
</body>
</html>
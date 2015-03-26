Quick note:
=====================================================================================================================
You can see the project running here http://omershatil.ddns.net/fish/movethefish.html
Basically it servers as a template for combining Java frameworks (Hibernate, Spring, Spring MVC, Servlets, etc.) with some JavaScript, AJAX, and JQuery. You could use the project as a template for how to use the correct annotations for Spring/Hibernte, how to set a servlet to run on Tomcat, etc.
In addition, it has some JQuery animation. In particular, I am happy with the way the fish turn. 
However, the code is not "production quality". It's just a demo.
I will soon also add some unit tests with EasyMock and, most importantly some Test Automation mini-framework that is useful for data-driven server automation.

What Does Move The Fish Do?
===========================
- Click on the arrows of the palette to expand it.
- Select a new fish from the palette and double-click anywhere in the aquarium.
- Double-click a fish to kill it.
- Drag-and-Drop a fish to a new location. The DB updates and the fish will continue from that position.
- Fish turn around in a way that somewhat gives an illusion of 3D.
- School of fish move and turn together. You can add fish to the school. You can also move fish away from the school.
- Server generates the data for the fish. So, two different users accross the planet should see the exact same screen. If one user moves a fish to a new location, a second user will see the fish move towards that new location.

Technologies/APIs/Tools used:
=============================
- Browser: Firefox/Chrome, Firebug
- IDE: Eclipse
- DB: MySQL
- Backend: Java, Spring MVC, Servlets, Tomcat, Spring Framework, Hibernate
- Front-End: HTML5/CSS3, JavaScript, AJAX, JQuery
- Test Automation (to be done): Python, Robot Framework

Known Problems:
===============
- Does not work with IE in-spite of use of JQuery. Need to look at why that is.
- School animation is not perfect. Could improve.
- If you move a fish away from the school it doesn't always know how to go back and/or faces the wrong direction.

Possible New Features:
======================
- When someone grabs a fish and drops it in a new location it panics and rushes forward when let go. Then it goes back to normal speed.
- External apps to be able to also cause the fish to move via JMS calls, and REST
- DB to keep track of fish that the users mark for tracking, in order to be able to display a map of movement of each fish in a separate window
- drop a worm and fish should chase it. first fish there, eats it. other fish turn away and go.
- from time to time, big fish will eat small fish.

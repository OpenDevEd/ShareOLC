Variables
========

Variables [should be set up through a 'strings' file or config file]:
* t1=15 minutes (initialise countdown time)
* t2=0 minute (end countdown at t2)
* t3=3 (time to detect satellites)
* s1=5 (minimum number of satellites in use needed)
* a1=100m (within medium accuracy)
* a2=5m (within hig accuracy)

Sequence
========

* Stage 1: Is there connectivity? Can the google location API retrieve the location? Add a x second timer (10 seconds) for the application to determine this. Display 'Please Wait'. If yes, go to Stage 4, if not continue to Stage 2. 

* Stage 2: No connectivity, no satellites detected. Message string: 'Please go outside' and Button 'I'm outside'. If button tapped, display 'Please wait'. Wait for a maximum of t3  minutes.
** If satellites detected (> 0) in t3 mins (irrespective of whether the button is pressed or not), move to Stage 3. 
** If t3 mins is over display 'Please try to go to an open area.' Wait for t3 minutes again.
    *** If 3 minutes over display 'Unable to detect location, Restart' Display Restart Button -> Go to Stage 1.

* Stage 3: Emphemerids should be obtained from satellite now. Satellites should be in use now. 
** If 'satellites in use' >= s1, tell the user to wait for 15 minutes (t1=15), start count down from 15 to 0 and Move to Stage 4.
** If 'satellites in use' < s1 display 'Please try to go to an open area.' Wait for t3 minutes. 
    *** If 3 minutes over display 'Unable to detect location, Restart' Display Restart Button -> Go to Stage 1.
 
* Stage 4: Increasing Accuracy
** If accuracy >= a2 and <a1, display "medium"
** If accuracy <= a2, display "high", and go to Stage 5
** If the accuracy does not reach <=a2 when countdown = t2, display a message: 
    *** If 'satellites in use' < s1 display 'Please try to go to an open area.' (and stay in Stage 4).
    *** If 'satellites in use' >= s1 display 'Cannot get high accuracy, please share location anyway.' (and go to Stage 5)
* Stage 5: Display share button.

Share
=====

When pressing 'share' the following text is made available to applications that can receive text intents:


SharePlusCode. Your Plus Code is 5G59VXMM+QFF (height:1585.60m; sat:8/9; acc:high,16.57m; sensor:16.57,2.00).
Google Maps: https://www.google.com/maps/place/5G59VXMM+QFF
OpenStreetMap: https://www.openstreetmap.org/#map=12/-26.1155737/27.9837321
Maps.Me: https://ge0.me/5G59VXMM+QFF
Plus Codes: https://plus.codes/5G59VXMM+QFF Get SharePlusCode at URL.

In this, "sensor:" contains additional GPS sensor data, such as raw values for horizontal and vertical accuracy. The items are separated by <space>, but could also be separated e.g. by ";", as long as that works. 

geo:37.7749,-122.4194

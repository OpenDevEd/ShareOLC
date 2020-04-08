Sequence
========

* Stage 1: Is there connectivity? Can the google location API retrieve the location? If yes, go to stage X, if not continue.
* Stage 2: No connectivity, no satellites detected. Tell the user to go outside.
* Stage 3: Satellites detected: The phone is now able to get the ephemerids. Tell the user to wait for 15 minutes (t1=15), count down from 15 to 1. (I don't know whether satellites are shown to be in use at this stage, and whether android can detect this.)
* Stage 4: Emphemerids should be obtained from satellite now. Satellites should be in use now. Tell the user to wait. (If 'satellites in use' < s1=6 display 'Please try to go to an open area.'
** If accuracy better than a1=100m, display "medium"
** If accuracy better then a2=10m, display "high", and go to next stage
** If the accuracy doesn't get to high within one minute (t2=1), display a message: 
** If 'satellites in use' < s1=6 display 'Please try to go to an open area.' (and stay in Stage 4).
** If 'satellites in use' >= s1=6 display 'Cannot get high accuracy, please share location anyway.' (and go to Stage 5)
* Stage 5: Display share button.

Variables [should be set up through a 'strings' file or config file]:
* t1=15 minutes
* t2=1 minute
* s1=5
* a1=100m
* a2=5m


Share
=====

When pressing 'share' the following text is made available to applications that can receive text intents:

ShareOLC. olc:5GRF2F3V+8MM  geo:37.7749,-122.4194 height:241m sat:10/17 acc:high,3m sensor:1,2,3,4 https://www.google.com/maps/search/-13.9966875,29.4898101 https://www.openstreetmap.org/#map=12/-13.9467/29.5151 

In this, "sensor:" contains additional GPS sensor data, such as raw values for horizontal and vertical accuracy. The items are separated by <space>, but could also be separated e.g. by ";", as long as that works. I'll provide links for Google Maps etc.  If possible, the following should be made available to apps that can receive geo intents:

geo:37.7749,-122.4194

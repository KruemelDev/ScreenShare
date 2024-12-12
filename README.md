# ScreenShare
## Simple screen sharing application written in Java

## Set up
- At least java version 17 is required
<ol>
  <li>Clone this repo</li>
  <li>Build the project by running: ./gradlew shadowjar</li>
</ol>

- ### Run the server

  <ol>
    <li>Navigate to /server/build/libs</li>
    <li>Run server using: java -jar server-all.jar {port}</li>
  </ol>
  
- ### Run the client
  <ol>
    <li>Navigate to /client/build/libs</li>
    <li>Run client using: java -jar client-all.jar or by double tapping the file</li>
  </ol>

## Functions
- Transfer screen with mouse cursor
- Adjust frame rate and quality of the transfered screen

## Not implemented
- Remote Mouse control
- Remote Keyboard control

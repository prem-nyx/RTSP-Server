## OpenCam Fork

This repository is a fork of
[pedroSG94/RTSP-Server](https://github.com/pedroSG94/RTSP-Server)
maintained for the OpenCam project.

The OpenCam fork includes RTSP authentication hardening intended to
require Basic authentication for supported RTSP request methods.

### Authentication tests

The test suite covers:

- Missing authorization
- Incorrect credentials
- Malformed authorization headers
- Successful authorization
- Unknown RTSP methods
- Authentication requirements across supported methods

Run the RTSP server unit tests with:

```bash
./gradlew :rtspserver:testDebugUnitTest

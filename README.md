Introduction
============

The ws-proxy module is a central soap aware access layer which relays messages between systems.
This relaying works in two directions; for services hosted internally (inbound mode) but also for services hosted externally were we are the client (outbound mode).
One can compare this with a traditional http forward/reverse proxy but instead of operating on application transport it goes one level higher in the stack and deals with application messages; in this case soap.

In outbound mode our internal services will (normally) use the wsproxy as a http forward proxy. The wsproxy will then deal with delivering the received message to the actual target. For inbound mode the module will act as a reverse proxy accepting incoming messages from external clients and relay them to our internal services.

For more information please see my blog entry:

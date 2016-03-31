Graylog2 output plugin Syslog
=============================

Based on bundled with Graylog syslog4j library.

Allows to send syslog messages with TCP or UDP formatted as plain text (classic), structured syslog (rfc 5424) or CEF (experimental).

## How to build

  * Use eclise to build + export as JAR.
  * Use mvn package to create package.

## How to use

  * Download graylog2-output-syslog.jar from releases and put inside /graylog-1.x/plugins folder
  * Restart Graylog2
  * Create new output globally or inside stream.

## How to configure

  * Protocol: use tcp or udp
  * Host: Hostname with syslog 
  * Port: Port for syslog, usually 514
  * Format: Specify one of plain, structured, full, cef or custom:FQCN (see below for explanation on values)
  
## Supported formats

### plain

Standard plain syslog format. Minimal information. 

### structured

Based on rfc5424. Sends all fields + log message.
Example:
````
<14>1 2016-03-31T19:19:02.524Z graylog unknown - nginx [all@0 request_verb="GET" remote_addr="192.168.1.37" response_status="404" from_nginx="true" level="6" connection_requests="1" http_version="1.1" response_bytes="1906" source="nginx" message="GET /test1/x HTTP/1.1" gl2_source_input="566c96abe4b094dfbc2661a8" version="1.1" nginx_access="true" http_user_agent="Wget/1.15 (linux-gnu)" remote_user="-" connection_id="970" http_referer="-" request_path="/test1/x" gl2_source_node="bebd092c-85d7-49a3-8188-f7af734747fb" _id="6d833da0-f775-11e5-b30c-0800276c97db" millis="0.002" facility="runit-service" timestamp="2016-03-31T19:19:02.000Z"] source: nginx | message: GET /test1/x HTTP/1.1 { request_verb: GET | remote_addr: 192.168.1.37 | response_status: 404 | from_nginx: true | level: 6 | connection_requests: 1 | http_version: 1.1 | response_bytes: 1906 | gl2_source_input: 566c96abe4b094dfbc2661a8 | version: 1.1 | nginx_access: true | http_user_agent: Wget/1.15 (linux-gnu) | remote_user: - | connection_id: 970 | http_referer: - | request_path: /test1/x | gl2_source_node: bebd092c-85d7-49a3-8188-f7af734747fb | _id: 6d833da0-f775-11e5-b30c-0800276c97db | millis: 0.002 | facility: runit-service | timestamp: 2016-03-31T19:19:02.000Z }
````

### cef

Common event format aka HP ArcSight format. This is Work in progress as I don`t have access to HP ArcSight instance. Please leave your feedback in issues.

### full

A variation of structured format except full message is added.

### custom:FQCN

Specify your implementation of com.wizecore.graylog2.plugin.MessageSender interface. 

## Links

  * https://tools.ietf.org/html/rfc5424
  * https://github.com/Graylog2
  * http://blog.rootshell.be/2011/05/11/ossec-speaks-arcsight/
  * https://groups.google.com/forum/#!topic/ossec-list/3guXmHJYHtY
  * http://habrahabr.ru/post/151631/
  * http://www.syslog4j.org/
  * https://www.graylog.org/resources/gelf-2/
  * http://docs.graylog.org/en/1.0/pages/plugins.html

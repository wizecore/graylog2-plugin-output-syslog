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

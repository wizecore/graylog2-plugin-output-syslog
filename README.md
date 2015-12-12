Graylog2 output plugin Syslog
=============================

Based on bundled with Graylog syslog4j library.

Allows to send syslog messages with TCP or UDP formatted as plain text (classic), structured syslog (rfc 5424) or CEF.

## How to build

  * Use eclise to build + export as JAR.
  * Use mvn package to create package.

## How to use

  * Download graylog2-output-syslog.jar from releases and put inside /graylog-1.x/plugins folder
  * Restart Graylog2
  * Create new output globally or inside stream.

## Links

  * https://tools.ietf.org/html/rfc5424
  * https://github.com/Graylog2
  * http://blog.rootshell.be/2011/05/11/ossec-speaks-arcsight/
  * https://groups.google.com/forum/#!topic/ossec-list/3guXmHJYHtY
  * http://habrahabr.ru/post/151631/
  * http://www.syslog4j.org/
  * https://www.graylog.org/resources/gelf-2/
  * http://docs.graylog.org/en/1.0/pages/plugins.html

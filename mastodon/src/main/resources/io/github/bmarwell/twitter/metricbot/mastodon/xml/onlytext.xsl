<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>
  <xsl:output method="text" indent="no"/>
  <xsl:template match="//text()[normalize-space(.) = '']">
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>
</xsl:stylesheet>

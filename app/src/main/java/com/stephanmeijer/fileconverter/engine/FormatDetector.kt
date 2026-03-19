package com.stephanmeijer.fileconverter.engine

object FormatDetector {
    private val inputFormatMap = mapOf(
        "md" to "markdown", "markdown" to "markdown", "mkd" to "markdown",
        "html" to "html", "htm" to "html", "tex" to "latex", "latex" to "latex",
        "rst" to "rst", "org" to "org", "docx" to "docx", "odt" to "odt",
        "epub" to "epub", "txt" to "markdown", "json" to "json", "csv" to "csv",
        "tsv" to "tsv", "ipynb" to "ipynb", "xml" to "docbook", "rtf" to "rtf",
        "typ" to "typst", "adoc" to "asciidoc", "wiki" to "mediawiki",
        "textile" to "textile", "t2t" to "t2t", "twiki" to "twiki",
        "creole" to "creole", "man" to "man",
    )

    val formatDisplayNames = mapOf(
        "markdown" to "Markdown", "html" to "HTML", "latex" to "LaTeX",
        "docx" to "Word (DOCX)", "odt" to "OpenDocument (ODT)", "epub" to "EPUB",
        "rst" to "reStructuredText", "org" to "Org Mode", "json" to "Pandoc JSON",
        "plain" to "Plain Text", "native" to "Pandoc Native", "docbook" to "DocBook",
        "rtf" to "RTF", "asciidoc" to "AsciiDoc", "ipynb" to "Jupyter Notebook",
        "typst" to "Typst", "pptx" to "PowerPoint (PPTX)", "csv" to "CSV",
        "mediawiki" to "MediaWiki", "textile" to "Textile", "commonmark" to "CommonMark",
        "gfm" to "GitHub Markdown", "man" to "Man Page", "opml" to "OPML",
        "haddock" to "Haddock", "creole" to "Creole", "jira" to "Jira Wiki",
        "context" to "ConTeXt", "texinfo" to "Texinfo", "revealjs" to "reveal.js",
        "beamer" to "Beamer (LaTeX)", "biblatex" to "BibLaTeX", "bibtex" to "BibTeX",
        "csljson" to "CSL JSON", "chunkedhtml" to "Chunked HTML",
        "icml" to "InCopy ICML", "tei" to "TEI XML", "markua" to "Markua",
        "dzslides" to "DZSlides", "slideous" to "Slideous", "slidy" to "Slidy",
        "s5" to "S5", "ms" to "Groff MS",
    )

    val commonInputFormats = listOf(
        "markdown", "html", "latex", "docx", "odt", "epub", "rst",
        "org", "csv", "json", "ipynb", "rtf", "asciidoc", "typst"
    )

    val commonOutputFormats = listOf(
        "html", "markdown", "latex", "docx", "odt", "epub", "rst",
        "plain", "org", "json", "ipynb", "rtf", "asciidoc", "typst",
        "pptx", "revealjs", "beamer"
    )

    fun detectFormat(fileName: String): String? {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return inputFormatMap[ext]
    }

    fun getDisplayName(formatId: String): String =
        formatDisplayNames[formatId] ?: formatId.replaceFirstChar { it.uppercase() }
}
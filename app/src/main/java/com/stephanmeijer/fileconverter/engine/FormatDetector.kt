package com.stephanmeijer.fileconverter.engine

object FormatDetector {

    enum class Category { AUDIO, VIDEO, DOCUMENT }

    private val inputFormatMap = mapOf(
        "md" to "markdown", "markdown" to "markdown", "mkd" to "markdown",
        "html" to "html", "htm" to "html", "tex" to "latex", "latex" to "latex",
        "rst" to "rst", "org" to "org", "docx" to "docx", "odt" to "odt",
        "epub" to "epub", "txt" to "markdown", "json" to "json", "csv" to "csv",
        "tsv" to "tsv", "ipynb" to "ipynb", "xml" to "docbook", "rtf" to "rtf",
        "typ" to "typst", "adoc" to "asciidoc", "wiki" to "mediawiki",
        "textile" to "textile", "t2t" to "t2t", "twiki" to "twiki",
        "creole" to "creole", "man" to "man",
        "mp3" to "mp3", "wav" to "wav", "flac" to "flac", "ogg" to "ogg",
        "opus" to "opus", "aac" to "aac", "m4a" to "m4a",
        "mp4" to "mp4", "mkv" to "mkv", "mov" to "mov", "webm" to "webm",
    )

    private val formatCategoryMap = mapOf(
        "mp3" to Category.AUDIO, "wav" to Category.AUDIO, "flac" to Category.AUDIO,
        "ogg" to Category.AUDIO, "opus" to Category.AUDIO, "aac" to Category.AUDIO,
        "m4a" to Category.AUDIO,
        "mp4" to Category.VIDEO, "mkv" to Category.VIDEO, "mov" to Category.VIDEO,
        "webm" to Category.VIDEO,
        "markdown" to Category.DOCUMENT, "html" to Category.DOCUMENT,
        "latex" to Category.DOCUMENT, "docx" to Category.DOCUMENT,
        "odt" to Category.DOCUMENT, "epub" to Category.DOCUMENT,
        "rst" to Category.DOCUMENT, "org" to Category.DOCUMENT,
        "json" to Category.DOCUMENT, "csv" to Category.DOCUMENT,
        "tsv" to Category.DOCUMENT, "ipynb" to Category.DOCUMENT,
        "docbook" to Category.DOCUMENT, "rtf" to Category.DOCUMENT,
        "typst" to Category.DOCUMENT, "asciidoc" to Category.DOCUMENT,
        "mediawiki" to Category.DOCUMENT, "textile" to Category.DOCUMENT,
        "t2t" to Category.DOCUMENT, "twiki" to Category.DOCUMENT,
        "creole" to Category.DOCUMENT, "man" to Category.DOCUMENT,
        "plain" to Category.DOCUMENT,
    )

    private val audioOutputFormats = listOf("mp3", "aac", "m4a", "wav", "flac", "ogg", "opus")

    private val videoOutputFormats = listOf("mp4", "mkv", "mov", "webm") + audioOutputFormats

    private val documentOutputFormats = listOf(
        "html", "markdown", "latex", "docx", "odt", "epub", "rst",
        "plain", "org", "json", "ipynb", "rtf", "asciidoc", "typst",
        "pptx", "revealjs", "beamer"
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

    fun categoryOf(format: String): Category? = formatCategoryMap[format]

    fun validOutputFormats(category: Category): List<String> = when (category) {
        Category.AUDIO -> audioOutputFormats
        Category.VIDEO -> videoOutputFormats
        Category.DOCUMENT -> documentOutputFormats
    }

    fun getDisplayName(formatId: String): String =
        formatDisplayNames[formatId] ?: formatId.replaceFirstChar { it.uppercase() }
}

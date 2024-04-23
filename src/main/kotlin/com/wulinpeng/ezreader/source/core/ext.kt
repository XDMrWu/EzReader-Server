package com.wulinpeng.ezreader.source.core

import org.jsoup.nodes.Element
import org.jsoup.select.Elements

fun Element.id(id: String): Element {
    return this.getElementById(id)
}

fun Element.clazz(clazz: String, index: Int = 0): Element {
    return this.getElementsByClass(clazz)[index]
}

fun Element.classes(clazz: String): Elements {
    return this.getElementsByClass(clazz)
}

fun Element.tag(tag: String, index: Int = 0): Element {
    return this.getElementsByTag(tag)[index]
}

fun Element.tags(tag: String): Elements {
    return this.getElementsByTag(tag)
}
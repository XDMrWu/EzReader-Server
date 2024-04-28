package com.wulinpeng.ezreader.route

import com.wulinpeng.ezreader.coze.CozeApi
import com.wulinpeng.ezreader.plugins.EzReaderRouteConfigure
import com.wulinpeng.ezreader.sse.SseEvent
import com.wulinpeng.ezreader.sse.server.respondSse
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import org.koin.core.annotation.Single

@Single
class ChatBotRoute: EzReaderRouteConfigure {
    override fun config(route: Route) {
        with(route) {
            get("/chatBot") {
                val query = context.request.queryParameters["query"]
                val fake = context.request.queryParameters["fake"].toBoolean()
                val flow = if (!fake) {
                    CozeApi.sendMessage(query ?: "").filter { !it.isNullOrEmpty() }.map {
                        SseEvent(data = defaultJson.encodeToString(SseData(it)))
                    }
                } else {
                    // 节省 Token，调试时返回 Fake 数据
                    flowOf(*fakeResponse.toList().toTypedArray()).map {
                        delay(100)
                        SseEvent(data = defaultJson.encodeToString(SseData(it.toString())))
                    }
                }

                call.respondSse(flow)
            }
        }
    }
}

@Serializable
data class SseData(val content: String)

const val fakeResponse = "当然！以下是几部你可能会喜欢的修真小说：\n\n\uD83D\uDCD6 小说标题: 《斗罗大陆》\n\uD83D\uDE0E 作者名称: 唐家三少\n\uD83D\uDCA1 小说概要: 这是一个关于斗罗大陆的故事，一个属于战斗和魂斗罗的世界。故事主要讲述了唐舞桐和唐三的修炼之路，以及他们在斗罗大陆上的冒险和成长。\n\uD83D\uDCA1 推介理由: 《斗罗大陆》不仅有丰富的修真元素，还注重角色的成长和智慧。主角唐三作为一个聪明绝顶的修真者，通过巧妙的策略和智慧的运用，在修炼和战斗中脱颖而出。\n\uD83C\uDF1F 精彩部分: \"只要换做其他元素上不克上海神龙岛的强者的话，哪怕是两颗三十四级魂核恐怕也不能互相进行搭配进化。但是我有神通这一项能力，有这个能力就可以为自己制订出更好的发展方向。\" - 《斗罗大陆》\n\n\uD83D\uDCD6 小说标题: 《大道朝天》\n\uD83D\uDE0E 作者名称: 蛮荒风\n\uD83D\uDCA1 小说概要: 这是一个宏大的修真世界，主角罗浩从修真初学者成长为强大的修真者的故事。在修真的道路上，他面临着各种挑战和考验。\n\uD83D\uDCA1 推介理由: 《大道朝天》中的主角罗浩非常聪明且有智慧，他通过自己的努力和洞察力，解决了许多问题，并逐渐成长为无敌的修真者。\n\uD83C\uDF1F 精彩部分: \"一只独脚鼎炖鱼头！不用多说，哦！是的，卖的正是这个。\" - 《大道朝天》\n\n希望你会喜欢以上小说！如果有需要其他类型的小说推荐，随时告诉我哦！"
package com.wulinpeng.ezreader.sse.client

import com.wulinpeng.ezreader.sse.EventData
import com.wulinpeng.ezreader.sse.EventId
import com.wulinpeng.ezreader.sse.EventType
import com.wulinpeng.ezreader.sse.SseEvent
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line

internal suspend inline fun ByteReadChannel.readSse(
    onSseEvent: (SseEvent) -> (Unit),
) {
    var id: EventId? = null
    var event: EventType? = null
    var data: EventData? = null

    while (!isClosedForRead) {
        parseSseLine(
            line = readUTF8Line(),
            onSseRawEvent = { sseRawEvent ->
                when (sseRawEvent) {
                    SseRawEvent.End -> {
                        if (data != null) {
                            onSseEvent(SseEvent(id, event, data!!))
                            id = null
                            event = null
                            data = null
                        } else {
                            // do nothing
                        }
                    }
                    is SseRawEvent.Id -> id = sseRawEvent.value
                    is SseRawEvent.Event -> event = sseRawEvent.value
                    is SseRawEvent.Data -> data = sseRawEvent.value
                    is SseRawEvent.Comment -> {
                        // do nothing
                    }
                    is SseRawEvent.Error -> {
                        // do nothing for now
                    }
                    is SseRawEvent.Retry -> {

                    }
                }
            }
        )
    }
}
package io.evanwong.oss.hipchat.v2

import io.evanwong.oss.hipchat.v2.commons.NoContent
import io.evanwong.oss.hipchat.v2.rooms.MessageColor
import io.evanwong.oss.hipchat.v2.rooms.Rooms
import spock.lang.Specification

class HipChatClientIntegrationSpec extends Specification {
    def client = new HipChatClient()
    //real token to be read from the "hipchat.token" property
    static String token

    def setupSpec() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug")
        token = System.getProperty("hipchat.token")
    }

    def "return all rooms for the valid access token"() {
        setup:
        def token = token
        def request = client.prepareGetAllRoomsRequestBuilder(token).build()


        when:
        Rooms rooms = request.execute().get()

        then:
        rooms.items.each {
            assert it.id != null
            assert it.name != null
            assert it.links != null
            assert it.links.self != null
        }
    }

    def "same access token should have the same behaviours"() {
        setup:
        def token = token
        def request1 = client.prepareGetAllRoomsRequestBuilder(token).build()
        def client2 = new HipChatClient()
        client2.setDefaultAccessToken(token)
        def request2 = client2.prepareGetAllRoomsRequestBuilder().build()
        Rooms rooms1 = request1.execute().get()
        Rooms rooms2 = request2.execute().get()

        expect:
        rooms1.items.eachWithIndex { item, index ->
            assert rooms2.items[index].id == item.id
            assert rooms2.items[index].links.self == item.links.self
        }
    }

    def "send notification return nothing"() {
        setup:
        def token = token
        def request = client.prepareSendRoomNotificationRequestBuilder(idOrName, message, token).setNotify(notify).setColor(color).build()

        when:
        NoContent noContent = request.execute().get()

        then:
        noContent != null

        where:
        idOrName | message    | notify | color
        "12345"  | "hello"    | true   | MessageColor.GRAY
        "test1"  | "hello123" | false  | MessageColor.GREEN

    }

    def "get all emoticons should return a list of emoticon"() {
        setup:
        def token = token
        def request = client.prepareGetAllEmoticonsRequestBuilder(token).build()

        when:
        def emoticons = request.execute().get()

        then:
        emoticons != null
        emoticons.maxResults == 100
        emoticons.startIndex == 0
        emoticons.links.next != null
        emoticons.links.self != null
        emoticons.items.size() > 0
        emoticons.items[0].url != null
        emoticons.items[0].id != null
        emoticons.items[0].shortcut != null
        emoticons.items[0].links.self != null
    }

    def "delete room should delete the room for the given id or name"() {
        setup:
        def testRoom = "testdelete"
        client.prepareCreateRoomRequestBuilder(testRoom, token).build().execute().get()
        def getBuilder = client.prepareGetRoomRequestBuilder(testRoom, token)
        def delBuilder = client.prepareDeleteRoomRequestBuilder(testRoom, token).build()

        when:
        def created = getBuilder.build().execute()?.get()?.id
        delBuilder.execute().get()

        then:
        created != null
        getBuilder.build().execute().get() == null

    }

}
package co.statu.parsek.api.event

import co.statu.parsek.api.ParsekEvent
import co.statu.parsek.model.Route

interface RouterEventListener : ParsekEvent {
    fun onInitRouteList(routes: MutableList<Route>)
}
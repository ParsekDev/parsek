package co.statu.parsek.api.event

import co.statu.parsek.model.Route

interface RouterEventListener : ParsekEventListener {

    fun onInitRouteList(routes: MutableList<Route>)
}
package co.statu.parsek.api.event

import co.statu.parsek.model.Route
import io.vertx.ext.web.Router

interface RouterEventListener : ParsekEventListener {

    fun onInitRouteList(routes: MutableList<Route>) {}

    fun onBeforeCreateRoutes(router: Router) {}
}
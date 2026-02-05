package com.ispw.controller.graphic.abstracts;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.NavigableController;

/**
 * Base astratta per i navigator CLI/GUI: mantiene la mappa route→controller e lo storico.
 * Non introduce logica di view; espone solo il routing tra controller grafici.
 */
public abstract class AbstractGraphicControllerNavigation implements GraphicControllerNavigation {

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    protected final Map<String, NavigableController> routes = new HashMap<>();
    protected final Deque<String> history = new ArrayDeque<>();
    protected String currentRoute;

    public void registerRoute(String route, NavigableController controller) {
        if (route == null || controller == null) {
            return;
        }
        routes.put(route, controller);
    }

    @Override
    public void goTo(String route, Map<String, Object> params) {
        if (route == null) {
            return;
        }

        NavigableController controller = routes.get(route);
        if (controller == null) {
            return;
        }

        if (currentRoute != null && !currentRoute.equals(route)) {
            history.push(currentRoute);
        }
        currentRoute = route;
        controller.onShow(params);
    }

    @Override
    public void back() {
        if (history.isEmpty()) {
            return;
        }
        String previous = history.pop();
        NavigableController controller = routes.get(previous);
        if (controller == null) {
            return;
        }
        currentRoute = previous;
        controller.onShow(Map.of());
    }

    @Override
    public void exit() {
        System.exit(0);
    }
}

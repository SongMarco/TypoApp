package nova.typoapp.groupChat.ottoEventBus;

import com.squareup.otto.Bus;

/**
 * Created by Administrator on 2018-02-23.
 */
public final class BusProvider {
    private static final CustomBus BUS = new CustomBus();

    public static Bus getInstance() {
        return BUS;
    }

    private BusProvider() {
        // No instances.
    }
}


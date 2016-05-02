package gigaherz.enderthing;

import java.util.UUID;

public interface IModProxy
{
    void preInit();

    void init();

    String queryNameFromUUID(UUID uuid);
}

package com.github.asgard.hermod.stream;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

public interface LiveSource {

    Set<String> liveLogins(Collection<String> logins) throws IOException, InterruptedException;
}

package sk.kamil.morley.utils.logger;

import timber.log.Timber;

/**
 * Created by Kamil on 9/19/2017.
 */

public class DebugTree extends Timber.DebugTree {

    @Override
    protected String createStackElementTag(StackTraceElement element) {
        return super.createStackElementTag(element)+":"+element.getLineNumber();
    }

}

package com.butel.data.analyses.mode.job;

import com.butel.data.analyses.mode.data.MongoData;
import com.butel.data.analyses.mode.protocol.DataPackage;

/**
 * Created by ninghf on 2017/12/14.
 */
public interface IAlgorithm {

    MongoData byteBufToMessage(DataPackage dataPackage);
}

package cn.edu.thu.tsfiledb.sys.writeLog;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import cn.edu.thu.tsfile.timeseries.read.qp.Path;
import cn.edu.thu.tsfile.timeseries.write.record.TSRecord;
import cn.edu.thu.tsfiledb.exception.PathErrorException;
import cn.edu.thu.tsfiledb.metadata.MManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.thu.tsfiledb.qp.physical.plan.PhysicalPlan;

public class WriteLogManager {
    private static final Logger LOG = LoggerFactory.getLogger(WriteLogManager.class);
    private static WriteLogManager instance;
    private static HashMap<String, WriteLogNode> logNodeMaps;
    public static final int BUFFERWRITER = 0, OVERFLOW = 1;
    
    private WriteLogManager() {
        logNodeMaps = new HashMap<>();
    }

    public static WriteLogManager getInstance() {
        if (instance == null) {
            synchronized (WriteLogManager.class) {
                instance = new WriteLogManager();
            }
        }
        return instance;
    }

    public static WriteLogNode getWriteLogNode(String fileNode) {
        if (logNodeMaps.containsKey(fileNode)) {
            return logNodeMaps.get(fileNode);
        }
        logNodeMaps.put(fileNode, new WriteLogNode(fileNode));
        return logNodeMaps.get(fileNode);
    }

    public void write(PhysicalPlan plan) throws IOException, PathErrorException {
        getWriteLogNode(MManager.getInstance().getFileNameByPath(plan.getPath().getFullPath())).write(plan);
    }

    public void write(TSRecord record, int type) throws IOException, PathErrorException {
    	getWriteLogNode(MManager.getInstance().getFileNameByPath(record.deltaObjectId)).write(record, type);
    }
    
    public void startOverflowFlush(String nsPath) throws IOException {
    	getWriteLogNode(nsPath).overflowFlushStart();
    }
    
    public void endOverflowFlush(String nsPath) throws IOException {
    	getWriteLogNode(nsPath).overflowFlushEnd();
    }
    
    public void startBufferWriteFlush(String nsPath) throws IOException {
    	getWriteLogNode(nsPath).bufferFlushStart();
    }
    
    public void endBufferWriteFlush(String nsPath) throws IOException {
    	getWriteLogNode(nsPath).bufferFlushEnd();
    }
    
    public PhysicalPlan getPhysicalPlan() throws IOException {
    	List<String> nsPathList = null;
		try {
			nsPathList = MManager.getInstance().getAllFileNames();
		} catch (PathErrorException e) {
			throw new IOException(e);
		}
		PhysicalPlan plan = null;
    	for(String nsPath : nsPathList){
    		WriteLogNode node = getWriteLogNode(nsPath);
    		plan = node.getPhysicalPlan();
    		if(plan != null){
    			return plan;
    		}
    	}
        return plan;
    }
}

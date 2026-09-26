package com.wxiwei.office.fc.hslf.record;

import java.util.Hashtable;

public interface PositionDependentRecord {
    public int getLastOnDiskOffset();
    public void setLastOnDiskOffset(int offset);
    public void updateOtherRecordReferences(Hashtable<Integer, Integer> oldToNewReferencesLookup);
    public void dispose();
}

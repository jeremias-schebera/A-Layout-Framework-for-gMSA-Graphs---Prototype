package main.java.application.Algorithmen.compactification;

import main.java.application.data.BlockSet;
import java.util.List;

public class Compactification {

    /// Input
    private BlockSet guideSequenceBlockSet;
    private List<BlockSet> blockSetList;

    /// Local
    private int guideBlockSetIndex = 0;
    //Test!!!
    private int maxSpecialShiftSteps = 0;
    //Test!!!

    /// Output
    // relative y-coordinate for every vertex

    AdjustVerticalDrawingPositions upperArea;
    AdjustVerticalDrawingPositions lowerArea;

    public Compactification(
        int maxLayer,
        BlockSet guideSequenceBlockSet,
        List<BlockSet> blockSetList
    ) {
        this.guideSequenceBlockSet = guideSequenceBlockSet;
        this.blockSetList = blockSetList;

        lowerArea = new AdjustVerticalDrawingPositions(maxLayer, CompactificationSide.LOWER);
        upperArea = new AdjustVerticalDrawingPositions(maxLayer, CompactificationSide.UPPER);
    }

    /** Last Sugiyama Step --> calculating drawing position for BlockSets

     */
    public void calculateVerticalDrawingPositions() {
        //find GuideSequenz index in BlockSetList
        for (int blockSetIndex = 0; blockSetIndex < blockSetList.size(); blockSetIndex++) {
            BlockSet blockSet = blockSetList.get(blockSetIndex);
            if (blockSet.equals(guideSequenceBlockSet)) {
                guideBlockSetIndex = blockSetIndex;
                break;
            }
        }

        //set GuideSequence on position 0
        blockSetList.get(guideBlockSetIndex).setDrawingPosition(0);

        // try to get the BlockSets under the GuideSequence as close as possible to the GuideSequence --> pull up
        for (int blockSetIndex = guideBlockSetIndex + 1; blockSetIndex < blockSetList.size(); blockSetIndex++) {
            lowerArea.shiftBlockSetsTowardsGuideSequence(blockSetList.get(blockSetIndex));
        }

        // try to get the BlockSets above the GuideSequence as close as possible to the GuideSequence --> pull down
        for (int blockSetIndex = guideBlockSetIndex - 1; blockSetIndex >= 0; blockSetIndex--) {
            upperArea.shiftBlockSetsTowardsGuideSequence(blockSetList.get(blockSetIndex));
        }

        //reposition of blocksets --> most upper blockSetPosition is 0
        for (BlockSet blockSet : blockSetList) {
            blockSet.setDrawingPosition(blockSet.getDrawingPosition() + upperArea.getDepth());
        }

        lowerArea.specialCaseShifting();
        upperArea.specialCaseShifting();

        //Test!!!
        maxSpecialShiftSteps = Math.max(lowerArea.getMaxSpecialShiftSteps(), upperArea.getMaxSpecialShiftSteps());
        //Test!!!
    }

    //Test!!!
    public StringBuffer csvLine(StringBuffer sb) {
        sb.append(maxSpecialShiftSteps + ";");
        return sb;
    }
    //Test!!!
}

/*******************************************************************************
 * Copyright (c) 2020 Jeremias Schebera, Dirk Zeckzer, Daniel Wiegreffe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package main.java.application.data;

import main.java.application.GUI.Rectangle;
import javafx.scene.text.Text;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.*;
import main.java.application.Algorithmen.GraphProjectionSugiyama;

public class VertexSugiyama {

    private String vertexText;
    private int id;

    private Rectangle rectangle;
    private String text;
    private List<EdgeSugiyama> inEdge = new LinkedList<>();
    private List<EdgeSugiyama> outEdge = new LinkedList<>();
    private boolean isDummyNode;
    private Layer associatedLayer;
    private Block associatedBlock;

    private int sequenceLength;
    private BlockSet associatedBlockSet;
    private boolean isJoinable;
    private boolean isJoinedVertex;
    private List<VertexSugiyama> joinedVertices;

    private EdgesClassifiedByVerticalDirection outEdgesClassifiedByVerticalDirection;
    private EdgesClassifiedByVerticalDirection inEdgesClassifiedByVerticalDirection;

    //JOIN-Test
    private int idPredecessorForJoining = -1;
    private int idSucessorForJoining = -1;
    //JOIN-Test

    //"standard" Vertex Constructer
    public VertexSugiyama(
        Vertex currentDBVertex,
        boolean isGuideSequence,
        int sequenceLength
    ) {
        this.id = currentDBVertex.value("id");
        vertexText = String.valueOf(this.id);
        this.sequenceLength = sequenceLength;
        isJoinable = true;
    }

    //"dummy" Vertex Constructer
    public VertexSugiyama(
        boolean isDummyNode,
        int id
    ) {
        this.isDummyNode = isDummyNode;
        this.id = -id;
        vertexText = String.valueOf(this.id);
        isJoinedVertex = false;
    }

    //"joined" Vertex Constructer
    public VertexSugiyama(
        int joinedVertexCount,
        List<VertexSugiyama> joinedVertices,
        List<VertexSugiyama> vertexList,
        int id
    ) {
        this.sequenceLength = 0;
        this.id = id;
        vertexText = joinedVertices.get(0).vertexText + "\n --> \n" + joinedVertices.get(joinedVertices.size() - 1).vertexText;
        this.joinedVertices = new ArrayList<>();
        for (VertexSugiyama includedVertex : joinedVertices) {
            this.sequenceLength += includedVertex.sequenceLength;
            this.joinedVertices.add(includedVertex);
            vertexList.remove(includedVertex);
        }
        this.isJoinedVertex = true;
        isDummyNode = false;
    }

    //JOIN-Test
    public int getIdPredecessorForJoining() {
        return idPredecessorForJoining;
    }

    public void setIdPredecessorForJoining(int idPredecessorForJoining) {
        this.idPredecessorForJoining = idPredecessorForJoining;
    }

    public int getIdSucessorForJoining() {
        return idSucessorForJoining;
    }

    public void setIdSucessorForJoining(int idSucessorForJoining) {
        this.idSucessorForJoining = idSucessorForJoining;
    }
    //JOIN-Test

    public int getSequenceLength() {
        return sequenceLength;
    }

    public void initializeOutEdgeClassifiedByVerticalDirection() {
        outEdgesClassifiedByVerticalDirection = new EdgesClassifiedByVerticalDirection();
    }

    public void initializeInEdgeClassifiedByVerticalDirection() {
        inEdgesClassifiedByVerticalDirection = new EdgesClassifiedByVerticalDirection();
    }

    public EdgesClassifiedByVerticalDirection getOutEdgesClassifiedByVerticalDirection() {
        return outEdgesClassifiedByVerticalDirection;
    }

    public EdgesClassifiedByVerticalDirection getInEdgesClassifiedByVerticalDirection() {
        return inEdgesClassifiedByVerticalDirection;
    }

    public boolean isJoinedVertex() {
        return isJoinedVertex;
    }

    public boolean isJoinable() {
        return isJoinable;
    }

    public void setJoinable(boolean joinable) {
        isJoinable = joinable;
    }

    public BlockSet getAssociatedBlockSet() {
        return associatedBlockSet;
    }

    public void setAssociatedBlockSet(BlockSet associatedBlockSet) {
        this.associatedBlockSet = associatedBlockSet;
    }

    public Block getAssociatedBlock() {
        return associatedBlock;
    }

    public void setAssociatedBlock(Block associatedBlock) {
        this.associatedBlock = associatedBlock;
    }

    public void setAssociatedLayer(Layer layer) {
        this.associatedLayer = layer;
    }

    public Layer getAssociatedLayer() {
        return associatedLayer;
    }

    public void addInEdge(EdgeSugiyama inEdge) {
        this.inEdge.add(inEdge);
    }

    public void removeInEdge(EdgeSugiyama inEdge) {
        this.inEdge.remove(inEdge);
    }

    public void addOutEdge(EdgeSugiyama outEdge) {
        this.outEdge.add(outEdge);
    }

    public void removeOutEdge(EdgeSugiyama outEdge) {
        this.outEdge.remove(outEdge);
    }

    public int getOutDegree() {
        return outEdge.size();
    }

    public List<EdgeSugiyama> getInEdges() {
        return inEdge;
    }

    public List<EdgeSugiyama> getOutEdges() {
        return outEdge;
    }


    public boolean isDummyNode() {
        return isDummyNode;
    }



    public Rectangle getRectangle() {
        return rectangle;
    }

    public void setRectangle(Rectangle rectangle) {
        this.rectangle = rectangle;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getVertexText() {
        return vertexText;
    }


    public Integer getId() {
        return id;
    }


    @Override
    public int hashCode() {
        return id;
    }
}

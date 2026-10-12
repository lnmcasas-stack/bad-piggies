package com.example.pigbuilder.client;

import java.util.List;

import com.example.pigbuilder.entity.ContraptionEntity.Cell;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class ContraptionRenderState extends EntityRenderState {
    public List<Cell> cells = List.of();
    public double sizeX, sizeZ;
    public float wheelDeg, propDeg;
    public boolean spinning;
}

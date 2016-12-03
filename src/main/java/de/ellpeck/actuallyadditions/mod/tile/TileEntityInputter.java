/*
 * This file ("TileEntityInputter.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2016 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.tile;


import de.ellpeck.actuallyadditions.mod.network.gui.IButtonReactor;
import de.ellpeck.actuallyadditions.mod.network.gui.INumberReactor;
import de.ellpeck.actuallyadditions.mod.util.StackUtil;
import de.ellpeck.actuallyadditions.mod.util.WorldUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class TileEntityInputter extends TileEntityInventoryBase implements IButtonReactor, INumberReactor{

    public static final int OKAY_BUTTON_ID = 133;
    public int sideToPut = -1;
    public int slotToPutStart;
    public int slotToPutEnd;
    public TileEntity placeToPut;
    public int sideToPull = -1;
    public int slotToPullStart;
    public int slotToPullEnd;
    public TileEntity placeToPull;
    public boolean isAdvanced;
    public FilterSettings leftFilter = new FilterSettings(12, true, true, false, false, 0, -1000);
    public FilterSettings rightFilter = new FilterSettings(12, true, true, false, false, 0, -2000);
    private int lastPutSide;
    private int lastPutStart;
    private int lastPutEnd;
    private int lastPullSide;
    private int lastPullStart;
    private int lastPullEnd;

    public TileEntityInputter(int slots, String name){
        super(slots, name);
    }

    public TileEntityInputter(){
        super(1, "inputter");
        this.isAdvanced = false;
    }

    @Override
    public void onNumberReceived(int text, int textID, EntityPlayer player){
        if(text != -1){
            if(textID == 0){
                this.slotToPutStart = Math.max(text, 0);
            }
            if(textID == 1){
                this.slotToPutEnd = Math.max(text, 0);
            }

            if(textID == 2){
                this.slotToPullStart = Math.max(text, 0);
            }
            if(textID == 3){
                this.slotToPullEnd = Math.max(text, 0);
            }
        }
        this.markDirty();
    }

    private boolean newPulling(){
        for(EnumFacing side : EnumFacing.values()){
            if(this.placeToPull.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)){
                IItemHandler cap = this.placeToPull.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
                if(cap != null){
                    for(int i = Math.max(this.slotToPullStart, 0); i < Math.min(this.slotToPullEnd, cap.getSlots()); i++){
                        if(this.checkBothFilters(cap.getStackInSlot(i), false)){
                            if(WorldUtil.doItemInteraction(i, 0, this.placeToPull, this, side, null)){
                                return true;
                            }
                        }
                    }
                }
            }

            if(this.placeToPull instanceof TileEntityItemViewer){
                break;
            }
        }
        return false;
    }

    private boolean newPutting(){
        if(this.checkBothFilters(this.slots.get(0), true)){
            for(EnumFacing side : EnumFacing.values()){
                if(this.placeToPut.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)){
                    IItemHandler cap = this.placeToPut.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
                    if(cap != null){
                        for(int i = Math.max(this.slotToPutStart, 0); i < Math.min(this.slotToPutEnd, cap.getSlots()); i++){
                            if(WorldUtil.doItemInteraction(0, i, this, this.placeToPut, null, side)){
                                return true;
                            }
                        }
                    }
                }

                if(this.placeToPut instanceof TileEntityItemViewer){
                    break;
                }
            }
        }
        return false;
    }

    /**
     * Checks if one of the filters contains the ItemStack
     *
     * @param stack The ItemStack
     * @return If the Item is filtered correctly
     */
    private boolean checkBothFilters(ItemStack stack, boolean output){
        return !this.isAdvanced || (output ? this.rightFilter : this.leftFilter).check(stack);
    }

    @Override
    public boolean shouldSaveDataOnChangeOrWorldStart(){
        return true;
    }

    /**
     * Sets all of the relevant variables
     */
    @Override
    public void saveDataOnChangeOrWorldStart(){
        if(this.sideToPull != -1){
            EnumFacing side = WorldUtil.getDirectionBySidesInOrder(this.sideToPull);
            this.placeToPull = this.world.getTileEntity(this.pos.offset(side));

            if(this.slotToPullEnd <= 0 && this.placeToPull != null){
                if(this.placeToPull instanceof IInventory){
                    this.slotToPullEnd = ((IInventory)this.placeToPull).getSizeInventory();
                }
                else{
                    if(this.placeToPull.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)){
                        IItemHandler cap = this.placeToPull.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                        if(cap != null){
                            this.slotToPullEnd = cap.getSlots();
                        }
                    }
                }
            }
        }

        if(this.sideToPut != -1){
            EnumFacing side = WorldUtil.getDirectionBySidesInOrder(this.sideToPut);
            this.placeToPut = this.world.getTileEntity(this.pos.offset(side));

            if(this.slotToPutEnd <= 0 && this.placeToPut != null){
                if(this.placeToPut instanceof IInventory){
                    this.slotToPutEnd = ((IInventory)this.placeToPut).getSizeInventory();
                }
                else{
                    if(this.placeToPut.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)){
                        IItemHandler cap = this.placeToPut.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                        if(cap != null){
                            this.slotToPutEnd = cap.getSlots();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onButtonPressed(int buttonID, EntityPlayer player){
        this.leftFilter.onButtonPressed(buttonID);
        this.rightFilter.onButtonPressed(buttonID);

        //Reset the Slots
        if(buttonID == 0 || buttonID == 1){
            this.slotToPutStart = 0;
            this.slotToPutEnd = 0;
        }
        if(buttonID == 2 || buttonID == 3){
            this.slotToPullStart = 0;
            this.slotToPullEnd = 0;
        }

        if(buttonID == 0){
            this.sideToPut++;
        }
        if(buttonID == 1){
            this.sideToPut--;
        }

        if(buttonID == 2){
            this.sideToPull++;
        }
        if(buttonID == 3){
            this.sideToPull--;
        }

        if(this.sideToPut >= 6){
            this.sideToPut = -1;
        }
        else if(this.sideToPut < -1){
            this.sideToPut = 5;
        }
        else if(this.sideToPull >= 6){
            this.sideToPull = -1;
        }
        else if(this.sideToPull < -1){
            this.sideToPull = 5;
        }

        this.markDirty();
        this.saveDataOnChangeOrWorldStart();
    }

    @Override
    public void writeSyncableNBT(NBTTagCompound compound, NBTType type){
        super.writeSyncableNBT(compound, type);
        if(type != NBTType.SAVE_BLOCK){
            compound.setInteger("SideToPut", this.sideToPut);
            compound.setInteger("SlotToPut", this.slotToPutStart);
            compound.setInteger("SlotToPutEnd", this.slotToPutEnd);
            compound.setInteger("SideToPull", this.sideToPull);
            compound.setInteger("SlotToPull", this.slotToPullStart);
            compound.setInteger("SlotToPullEnd", this.slotToPullEnd);
        }

        this.leftFilter.writeToNBT(compound, "LeftFilter");
        this.rightFilter.writeToNBT(compound, "RightFilter");
    }

    @Override
    public void readSyncableNBT(NBTTagCompound compound, NBTType type){
        if(type != NBTType.SAVE_BLOCK){
            this.sideToPut = compound.getInteger("SideToPut");
            this.slotToPutStart = compound.getInteger("SlotToPut");
            this.slotToPutEnd = compound.getInteger("SlotToPutEnd");
            this.sideToPull = compound.getInteger("SideToPull");
            this.slotToPullStart = compound.getInteger("SlotToPull");
            this.slotToPullEnd = compound.getInteger("SlotToPullEnd");
        }

        this.leftFilter.readFromNBT(compound, "LeftFilter");
        this.rightFilter.readFromNBT(compound, "RightFilter");

        super.readSyncableNBT(compound, type);
    }

    @Override
    public void updateEntity(){
        super.updateEntity();
        if(!this.world.isRemote){

            //Is Block not powered by Redstone?
            if(!this.isRedstonePowered){
                if(!(this.sideToPull == this.sideToPut && this.slotToPullStart == this.slotToPutStart && this.slotToPullEnd == this.slotToPutEnd)){
                    if(this.sideToPull != -1 && this.placeToPull != null){
                        this.newPulling();
                    }
                    if(StackUtil.isValid(this.slots.get(0)) && this.sideToPut != -1 && this.placeToPut != null){
                        this.newPutting();
                    }
                }
            }

            //Update the Client
            if((this.sideToPut != this.lastPutSide || this.sideToPull != this.lastPullSide || this.slotToPullStart != this.lastPullStart || this.slotToPullEnd != this.lastPullEnd || this.slotToPutStart != this.lastPutStart || this.slotToPutEnd != this.lastPutEnd || this.leftFilter.needsUpdateSend() || this.rightFilter.needsUpdateSend()) && this.sendUpdateWithInterval()){
                this.lastPutSide = this.sideToPut;
                this.lastPullSide = this.sideToPull;
                this.lastPullStart = this.slotToPullStart;
                this.lastPullEnd = this.slotToPullEnd;
                this.lastPutStart = this.slotToPutStart;
                this.lastPutEnd = this.slotToPutEnd;
                this.leftFilter.updateLasts();
                this.rightFilter.updateLasts();
            }
        }
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack stack){
        return i == 0;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, EnumFacing side){
        return this.isItemValidForSlot(slot, stack);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, EnumFacing side){
        return slot == 0;
    }
}
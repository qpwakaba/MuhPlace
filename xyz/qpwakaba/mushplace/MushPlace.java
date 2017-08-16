package xyz.qpwakaba.mushplace;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.*;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Torch;
import org.bukkit.metadata.*;

public class MushPlace extends JavaPlugin implements Listener {
    
    private List<Material> canPlaceItems;
    private List<Material> canPlaceToFlowerPotItems;
    private Map<Material, Material> itemToBlock;
    private Map<Material, Byte> itemToBlockDamage;
    private static final Class blockClass = getClass(getNMSPackageName() + ".Block");
    private Field tickingFlagField;
    private static final Method getBlockByIdMethod;
    private static final Field blockByIdField = getField(blockClass, "byId");
    static {
        Method temp = getMethod(blockClass, "getById", int.class);
        if(temp == null) {
            temp = getMethod(blockClass, "e", int.class);
        }
        getBlockByIdMethod = temp;
    }
    public void onEnable() {
        if(!this.isEnabled()) return;
        this.getServer().getConsoleSender().sendMessage("[MushPlace] " + ChatColor.AQUA + "作物のランダムドロップのあたりをいじっているので挙動がおかしくなっている可能性があります。");
        this.getServer().getConsoleSender().sendMessage("[MushPlace] " + ChatColor.AQUA + "もし、何か気づいた場合はKingさんに教えてやってください。");
        this.getServer().getConsoleSender().sendMessage("[MushPlace] " + ChatColor.AQUA + "詳しくは " + ChatColor.RED + "readme.txt" + ChatColor.AQUA + "を読んでください。");
        
        this.canPlaceItemsInit();
        this.itemToBlockInit();
        this.itemToBlockDamageInit();
        this.applyTickingFlag(canPlaceItems);
        this.getServer().getPluginManager().registerEvents(this, this);
    }
    public void onDisable() {
    }
    
    private void applyTickingFlag(Collection<Material> list) {
        if(tickingFlagField == null) {
            switch(getNMSMinorVersion()) {
                case 6:
                    //1.6用
                    tickingFlagField = getField(blockClass, "cK");
                    break;
                case 7:
                case 8:
                    //1.7～1.8用の処理
                    tickingFlagField = getField(blockClass, "z");
                    break;
                case 9:
                case 10:
                case 11:
                default:
                    //1.9
                    tickingFlagField = getField(blockClass, "t");
            }
        }
        if(tickingFlagField == null) {
            this.getServer().getConsoleSender().sendMessage(ChatColor.RED + "致命的なエラー: お使いのバージョンには対応していません。Kingさんに要望を送ってください。");
            this.setEnabled(false);
            return;
        }
        
        for(Material type: list) {
            Object block = getNMSBlock(type);
            if(block != null) {
                setTickingFlag(block, false);
            }
        }
    }
    private Object getNMSBlock(Material type) {
        return getNMSBlockById(type.getId());
    }
    
    private Object getNMSBlockById(int id) {
        if(getNMSMinorVersion() > 6) {
            return invokeMethod(getBlockByIdMethod, null, id);
        } else {
            return ((Object[]) getFieldValue(blockByIdField, null))[id];
        }
    }
    
    private void setTickingFlag(Object block, boolean flag) {
        setFieldValue(tickingFlagField, block, flag);
    }
    
    private void canPlaceItemsInit() {
        this.canPlaceItems = new ArrayList<>();
        this.canPlaceToFlowerPotItems = new ArrayList<>();
        Material[] temp = new Material[] {
                                          Material.SAPLING, 
                                          Material.LONG_GRASS, 
                                          Material.DEAD_BUSH, 
                                          Material.YELLOW_FLOWER, 
                                          Material.RED_ROSE, 
                                          Material.BROWN_MUSHROOM, 
                                          Material.RED_MUSHROOM, 
                                          Material.TORCH, 
                                          Material.CROPS, 
                                          Material.REDSTONE_TORCH_OFF, 
                                          Material.REDSTONE_TORCH_ON, 
                                          Material.STONE_BUTTON, 
                                          Material.CACTUS, 
                                          Material.SUGAR_CANE_BLOCK, 
                                          Material.PUMPKIN_STEM, 
                                          Material.MELON_STEM, 
                                          Material.VINE, 
                                          Material.WATER_LILY, 
                                          Material.NETHER_WARTS, 
                                          Material.COCOA, 
                                          Material.TRIPWIRE_HOOK, 
                                          Material.FLOWER_POT, 
                                          Material.CARROT, 
                                          Material.POTATO, 
                                          Material.WOOD_BUTTON, 
                                          Material.CARPET, 
                                          Material.SEEDS, 
                                          Material.IRON_DOOR, 
                                          Material.WOOD_DOOR,
                                          Material.ACTIVATOR_RAIL, 
                                          Material.DETECTOR_RAIL, 
                                          Material.POWERED_RAIL, 
                                          Material.RAILS, 
                                          Material.STONE_PLATE, 
                                          Material.WOOD_PLATE, 
                                          Material.IRON_PLATE, 
                                          Material.GOLD_PLATE, 
        };
        canPlaceItems.addAll(Arrays.asList(temp));
        temp = new Material[] {
                                          Material.SAPLING, 
                                          Material.DEAD_BUSH, 
                                          Material.YELLOW_FLOWER, 
                                          Material.RED_ROSE, 
                                          Material.BROWN_MUSHROOM, 
                                          Material.RED_MUSHROOM, 
                                          Material.CACTUS, 
        };
        this.canPlaceToFlowerPotItems.addAll(Arrays.asList(temp));
        if(getNMSMinorVersion() >= 8) {
            Material[] temp_after1_8 = new Material[] {
                                          Material.ACACIA_DOOR, 
                                          Material.BIRCH_DOOR, 
                                          Material.DARK_OAK_DOOR, 
                                          Material.JUNGLE_DOOR, 
                                          Material.SPRUCE_DOOR, 
            };
            canPlaceItems.addAll(Arrays.asList(temp_after1_8));
        }
        
        
    }
    
    private void itemToBlockInit() {
        this.itemToBlock = new HashMap<>();
        this.itemToBlock.put(Material.SEEDS, Material.CROPS);
        this.itemToBlock.put(Material.WHEAT, Material.CROPS);
        this.itemToBlock.put(Material.SUGAR_CANE, Material.SUGAR_CANE_BLOCK);
        this.itemToBlock.put(Material.PUMPKIN_SEEDS, Material.PUMPKIN_STEM);
        this.itemToBlock.put(Material.MELON_SEEDS, Material.MELON_STEM);
        this.itemToBlock.put(Material.NETHER_STALK, Material.NETHER_WARTS);
        this.itemToBlock.put(Material.CARROT_ITEM, Material.CARROT);
        this.itemToBlock.put(Material.POTATO_ITEM, Material.POTATO);
        this.itemToBlock.put(Material.FLOWER_POT_ITEM, Material.FLOWER_POT);
        this.itemToBlock.put(Material.IRON_DOOR, Material.IRON_DOOR_BLOCK);
        this.itemToBlock.put(Material.WOOD_DOOR, Material.WOODEN_DOOR);
        if(getNMSMinorVersion() >= 8) {
            this.itemToBlock.put(Material.ACACIA_DOOR_ITEM, Material.ACACIA_DOOR);
            this.itemToBlock.put(Material.BIRCH_DOOR_ITEM, Material.BIRCH_DOOR);
            this.itemToBlock.put(Material.DARK_OAK_DOOR_ITEM, Material.DARK_OAK_DOOR);
            this.itemToBlock.put(Material.JUNGLE_DOOR_ITEM, Material.JUNGLE_DOOR);
            this.itemToBlock.put(Material.SPRUCE_DOOR_ITEM, Material.SPRUCE_DOOR);
        }
    }
    
    private void itemToBlockDamageInit() {
        this.itemToBlockDamage = new HashMap<>();
        this.itemToBlockDamage.put(Material.WHEAT, (byte)7);
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        switch(event.getAction()) {
            case RIGHT_CLICK_BLOCK:
            case RIGHT_CLICK_AIR:
                //処理する
                break;
            default:
                //それ以外
                return;
        }
        if(event.getPlayer().getGameMode() != GameMode.CREATIVE) return;
        //if(event.useInteractedBlock() == org.bukkit.event.Event.Result.ALLOW) return;
        //this.getLogger().info("useInteractedBlock() == " + event.useInteractedBlock());
        //this.getLogger().info("useItemInHand() == " + event.useItemInHand());
        //if(event.useItemInHand() == org.bukkit.event.Event.Result.ALLOW) return;
        ItemStack itemStack = event.getItem();
        Material type = itemStack.getType();
        Block target;
        if(event.hasBlock() && event.getClickedBlock().getType() == Material.FLOWER_POT && this.canPlaceToFlowerPotItems.contains(type)) {
            //植木鉢にセットするため、キャンセルせずに抜ける
            return;
        }
        if(canPlaceItems.contains(getBlockByItemStack(itemStack))) {
            if(event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR) {
                List<Block> blocks = event.getPlayer().getLineOfSight((HashSet<Byte>)null, 5);
                target = blocks.get(blocks.size() - 1);
                if(target.isEmpty()) return;//空中クリックの設置防止
                for(int i = 2; !target.isEmpty() && i <= blocks.size(); i++) {
                    target = blocks.get(blocks.size() - i);
                }
            } else {
                target = event.getClickedBlock().getRelative(event.getBlockFace());
            }
            target.setTypeId(getBlockByItemStack(itemStack).getId(), false);//アイテムをブロックに変換
            byte data = getDataByItemStack(itemStack);
            //アイテムにブロックの時のダメージ値が決まってたらそっちにする。
            //そうじゃなければ持ってる奴のダメージ値使う。
            data = (data == -1 ? itemStack.getData().getData() : data);
            target.setData(data);
            event.setUseInteractedBlock(org.bukkit.event.Event.Result.ALLOW);
            
            //音鳴らす
            Location location = event.getClickedBlock().getLocation();
            //String soundName = Block.stepSound.getPlaceSound();
            //float volume1 = Block.stepSound.getVolume1();
            //float volume2 = Block.stepSound.getVolume2();
            //world.makeSound((double) location.getBlockX() + 0.5, (double) location.getBlockY() + 0.5, (double) location.getBlockZ() + 0.5,
            //                 soundName, (volume1 + 1.0F) / 2.0F, volume2 * 0.8F);
            
            //2箇所に設置されるのを防ぐ
            event.setCancelled(true);
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Block b = event.getBlock();
        if(canPlaceItems.contains(b.getType()) || (this.itemToBlock.containsKey(b.getType()) ? canPlaceItems.contains(this.itemToBlock.get(b.getType())) : false)) {
            switch(b.getType()) {
                case REDSTONE_TORCH_OFF:
                case REDSTONE_TORCH_ON:
                    if(b.getRelative(((Torch)b.getState().getData()).getAttachedFace()).getType().isOccluding()) return;
                    //固形ブロックだったらキャンセルの必要がないのでリターンする
            }
            event.setCancelled(true);
        }
    }
    
    private Material getBlockByItemStack(ItemStack itemStack) {
        Material type = itemStack.getType();
        return (itemToBlock.containsKey(type) ? itemToBlock.get(type) : type);
    }
    
    private byte getDataByItemStack(ItemStack itemStack) {
        Material type = itemStack.getType();
        byte lotMeta = getDataByLotMetaItem(itemStack);
        return (lotMeta != -1 ? 
            lotMeta :
                (
                    itemToBlockDamage.containsKey(type) ? 
                        itemToBlockDamage.get(type).byteValue() : 
                        -1
                )
            );
    }
    
    private String getPlaceSoundName(Material block) {
        //Object block = //Block.getById(block.getId());
        return null;
    }
    
    private final String PREFIX_LOTMETA = "LotMeta:";
    private byte getDataByLotMetaItem(ItemStack itemStack) {
        //LotMeta:
        ItemMeta itemMeta = itemStack.getItemMeta();
        if(itemMeta.getLore() != null)
        for(String lore: itemMeta.getLore()) {
            if(lore.startsWith(PREFIX_LOTMETA)) {
                String metaString = lore.substring(PREFIX_LOTMETA.length());
                try {
                    byte meta = Byte.parseByte(metaString);
                    return meta;
                } catch (NumberFormatException ex) {}
            }
        }
        return -1;
    }
    
    private static final String PREFIX_NMS_PACKAGE = "net.minecraft.server";
    private static final String PREFIX_OBC_PACKAGE = "org.bukkit.craftbukkit";
    public static String getNMSPackageName() {
        return getOBCPackageName().replace(PREFIX_OBC_PACKAGE, PREFIX_NMS_PACKAGE);
    }
    public static String getOBCPackageName() {
        return Bukkit.getServer().getClass().getPackage().getName();
    }
    public static String getNMSVersion() {
        return getOBCPackageName().replace(PREFIX_OBC_PACKAGE + ".", "");
    }
    
    public static final String PREFIX_NMS_VERSION_6 = "v1_6_R";
    public static final String PREFIX_NMS_VERSION_7 = "v1_7_R";
    public static final String PREFIX_NMS_VERSION_8 = "v1_8_R";
    public static final String PREFIX_NMS_VERSION_9 = "v1_9_R";
    public static final String PREFIX_NMS_VERSION_10 = "v1_10_R";
    public static final String PREFIX_NMS_VERSION_11 = "v1_11_R";
    private static final String REGEX_NMS_VERSION = "v(\\d+)_(\\d+)_R(\\d+)";
    private static final Matcher REGEX_NMS_VERSION_MATCHER = Pattern.compile(REGEX_NMS_VERSION).matcher(getNMSVersion());
    static {
            REGEX_NMS_VERSION_MATCHER.matches();
    }
    private static final int NMS_VERSION_MAJOR = Integer.parseInt(REGEX_NMS_VERSION_MATCHER.group(1));
    private static final int NMS_VERSION_MINOR = Integer.parseInt(REGEX_NMS_VERSION_MATCHER.group(2));
    private static final int NMS_VERSION_REVISION = Integer.parseInt(REGEX_NMS_VERSION_MATCHER.group(3));
    
    public static int getNMSMajorVersion() {
        return NMS_VERSION_MAJOR;
    }
    
    public static int getNMSMinorVersion() {
        return NMS_VERSION_MINOR;
    }
    
    public static int getNMSRevision() {
        return NMS_VERSION_REVISION;
    }
    
    private static Class getClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }
    
    private static Field getField(Class c, String field) {
        try {
            return c.getDeclaredField(field);
        } catch (NoSuchFieldException ex) {
            return null;
        }
    }
    
    private static Method getMethod(Class c, String method, Class... argsType) {
        try {
            return c.getDeclaredMethod(method, argsType);
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }
    
    private static Object getFieldValue(Field field, Object obj) {
        Object result;
        boolean accessible = field.isAccessible();
        try {
            field.setAccessible(true);
        } catch (SecurityException ex) {
            throw new IllegalStateException(ex);
        }
        try {
            result = field.get(obj);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (IllegalAccessException | ExceptionInInitializerError ex) {
            throw new IllegalStateException(ex);
        } finally {
            try {
                field.setAccessible(accessible);
            } catch (SecurityException ex) {
                throw new IllegalStateException(ex);
            }
        }
        
        return result;
    }
    
    private static void setFieldValue(String field, Object obj, Object value) {
        setFieldValue(getField(obj.getClass(), field), obj, value);
    }
    private static void setFieldValue(Field field, Object obj, Object value) {
        boolean accessible = field.isAccessible();
        try {
            field.setAccessible(true);
        } catch (SecurityException ex) {
            throw new IllegalStateException(ex);
        }
        try {
            field.set(obj, value);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (IllegalAccessException | ExceptionInInitializerError ex) {
            throw new IllegalStateException(ex);
        } finally {
            try {
                field.setAccessible(accessible);
            } catch (SecurityException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }
    
    private static Object invokeMethod(Method method, Object obj, Object... args) {
        Object result;
        boolean accessible = method.isAccessible();
        try {
            method.setAccessible(true);
        } catch (SecurityException ex) {
            throw new IllegalStateException(ex);
        }
        try {
            result = method.invoke(obj, args);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (IllegalAccessException | ExceptionInInitializerError | InvocationTargetException ex) {
            throw new IllegalStateException(ex);
        } finally {
            try {
                method.setAccessible(accessible);
            } catch (SecurityException ex) {
                throw new IllegalStateException(ex);
            }
        }
        
        return result;
    }
    
    
}
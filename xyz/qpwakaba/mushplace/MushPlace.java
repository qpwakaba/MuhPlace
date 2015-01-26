package xyz.qpwakaba.mushplace;
import java.lang.reflect.*;
import java.util.*;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.*;
import org.bukkit.inventory.ItemStack;

public class MushPlace extends JavaPlugin implements Listener {
	
	private List<Material> canPlaceItems;
	private Map<Material, Material> itemToBlock;
	private Map<Material, Byte> itemToBlockDamage;
	public void onEnable() {
		this.cropInit();
		if(!this.isEnabled()) return;
		this.getServer().getConsoleSender().sendMessage("[MushPlace] " + ChatColor.AQUA + "�앨�̃����_���h���b�v�̂�������������Ă���̂ŋ��������������Ȃ��Ă���\��������܂��B");
		this.getServer().getConsoleSender().sendMessage("[MushPlace] " + ChatColor.AQUA + "�����A�����C�Â����ꍇ��King����ɋ����Ă���Ă��������B");
		this.getServer().getConsoleSender().sendMessage("[MushPlace] " + ChatColor.AQUA + "�ڂ����� " + ChatColor.GOLD + "readme.txt" + ChatColor.AQUA + "��ǂ�ł��������B");
		
		this.canPlaceItemsInit();
		this.itemToBlockInit();
		this.itemToBlockDamageInit();
		this.getServer().getPluginManager().registerEvents(this, this);
	}
	public void onDisable() {
	}
	
	private void cropInit() {
		Class Block = getClass(getMinecraftPackageName() + ".Block");
		Field tickingFlag;
		Object cropObject, pumpkinObject, melonObject, saplingObject, cactusObject, caneObject, lilyObject;
		if(Block == null || getClass(getMinecraftPackageName() + ".BlockCrops") == null) {
			this.getServer().getConsoleSender().sendMessage(ChatColor.RED + "�v���I�ȃG���[: ���g���̃o�[�W�����ɂ͑Ή����Ă��܂���BKing����ɗv�]�𑗂��Ă��������B");
			this.setEnabled(false);
			return;
		}

		//1.6�p�̏���
		Field cropField = getField(Block, "CROPS");
		Field pumpkinField = getField(Block, "PUMPKIN_STEM");
		Field melonField = getField(Block, "MELON_STEM");
		Field saplingField = getField(Block, "SAPLING");
		Field cactusField = getField(Block, "CACTUS");
		Field caneField = getField(Block, "SUGAR_CANE_BLOCK");
		Field lilyField = getField(Block, "WATER_LILY");
		if(cropField != null) {
			cropObject = getFieldValue(cropField, null);
			pumpkinObject = getFieldValue(pumpkinField, null);
			melonObject = getFieldValue(melonField, null);
			saplingObject = getFieldValue(saplingField, null);
			cactusObject = getFieldValue(cactusField, null);
			caneObject = getFieldValue(caneField, null);
			lilyObject = getFieldValue(lilyField, null);
			tickingFlag = getField(Block, "cK");//1.6�p
		} else { //1.7�`1.8�p�̏���
			Field regField = getField(Block, "REGISTRY");
			if(regField != null) { 
				Class regClass = getClass(getMinecraftPackageName() + ".RegistryMaterials");
				Object reg = getFieldValue(regField, null);
				
				if(reg == null) {
					this.getServer().getConsoleSender().sendMessage(ChatColor.RED + "�v���I�ȃG���[: ���g���̃o�[�W�����A�������͊��ɑΉ����Ă��܂���BKing����ɗv�]�𑗂��Ă��������B");
					this.setEnabled(false);
					return;
				}
				
				Method getMethod = getMethod(regClass, "get", String.class);
				if(getMethod == null) {
					getMethod = getMethod(regClass, "a", String.class);
					if(getMethod == null) {
						getMethod = getMethod(regClass, "get", Object.class);
						this.getServer().getConsoleSender().sendMessage(ChatColor.RED + "�v���I�ȃG���[: " + (getMethod == null ? "���g���̃o�[�W����" : "�o�[�W����1.8�A�܂��͂���ȍ~�̃o�[�W����") + "�ɂ͑Ή����Ă��܂���BKing����ɗv�]�𑗂��Ă��������B");
						this.setEnabled(false);
						return;
					}
				}
				saplingObject = invokeMethod(getMethod, reg, "sapling");
				cropObject = invokeMethod(getMethod, reg, "wheat");
				pumpkinObject = invokeMethod(getMethod, reg, "pumpkin_stem");
				melonObject = invokeMethod(getMethod, reg, "melon_stem");
				cactusObject = invokeMethod(getMethod, reg, "cuctus");
				caneObject = invokeMethod(getMethod, reg, "reeds");
				lilyObject = invokeMethod(getMethod, reg, "waterlily");
				tickingFlag = getField(Block, "z");
			} else {
				this.getServer().getConsoleSender().sendMessage(ChatColor.RED + "�v���I�ȃG���[: ���g���̃o�[�W�����ɂ͑Ή����Ă��܂���BKing����ɗv�]�𑗂��Ă��������B");
				this.setEnabled(false);
				return;
			}
		}
		
		
		if(tickingFlag == null) {
			this.getServer().getConsoleSender().sendMessage(ChatColor.RED + "�v���I�ȃG���[: ���g���̃o�[�W�����ɂ͑Ή����Ă��܂���BKing����ɗv�]�𑗂��Ă��������B");
			this.setEnabled(false);
			return;
		}
		setFieldValue(tickingFlag, saplingObject, false);
		setFieldValue(tickingFlag, cropObject, false);
		setFieldValue(tickingFlag, pumpkinObject, false);
		setFieldValue(tickingFlag, melonObject, false);
		setFieldValue(tickingFlag, cactusObject, false);
		setFieldValue(tickingFlag, caneObject, false);
		setFieldValue(tickingFlag, lilyObject, false);
		
	}
	
	private void canPlaceItemsInit() {
		this.canPlaceItems = new ArrayList<>();
		Material[] temp = new Material[] {
		                                  Material.SAPLING, 
		                                  Material.LONG_GRASS, 
		                                  Material.DEAD_BUSH, 
		                                  Material.YELLOW_FLOWER, 
		                                  Material.RED_ROSE, 
		                                  Material.BROWN_MUSHROOM, 
		                                  Material.RED_MUSHROOM, 
		                                  Material.CROPS, 
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
		};
		/*try {
			Material[] temp_after1.7 = new Material[] {};
			canPlaceItems.AddAll(Arrays.asList(temp_after1.7));
		} catch (Exception ex) {
			//1.6�ȉ���1.7�ȏ�̂����낤�Ƃ������p
			//��{�I�ɂ�1.6����ō��
			//�v�]�������1.4.7�Ή��ł��B�����������낤���ǁB
		}*/
		canPlaceItems.addAll(Arrays.asList(temp));
		
		
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
				//��������
				break;
			default:
				//����ȊO
				return;
		}
		if(event.getPlayer().getGameMode() != GameMode.CREATIVE) return;
		if(event.useInteractedBlock() == org.bukkit.event.Event.Result.ALLOW) return;
		ItemStack item = event.getPlayer().getItemInHand();
		Material type = item.getType();
		if(canPlaceItems.contains(getBlockByItem(type))) {
			List<Block> blocks = event.getPlayer().getLineOfSight(null, 5);
			Block target = blocks.get(blocks.size() - 1);
			if(target.isEmpty()) return;//�󒆃N���b�N�̐ݒu�h�~
			for(int i = 2; !target.isEmpty() && i <= blocks.size(); i++) {
				target = blocks.get(blocks.size() - i);
			}
			target.setType(getBlockByItem(type));//�A�C�e�����u���b�N�ɕϊ�
			byte data = getDataByItem(type);
			//�A�C�e���Ƀu���b�N�̎��̃_���[�W�l�����܂��Ă��炻�����ɂ���B
			//��������Ȃ���Ύ����Ă�z�̃_���[�W�l�g���B
			target.setData(data == 0 ? (byte)(item.getDurability() & 0xFF) : data);
			event.setUseInteractedBlock(org.bukkit.event.Event.Result.ALLOW);
			//���炷
			
		}
	}
	
	@EventHandler
	public void onBlockPhysics(BlockPhysicsEvent event) {
		if(canPlaceItems.contains(event.getBlock().getType())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockGrow(BlockGrowEvent event) {
		this.getLogger().info(event.getEventName());
		this.getLogger().info(event.getBlock().getLocation().toString());
		this.getLogger().info(event.getNewState().toString());
	}
	private Material getBlockByItem(Material item) {
		return (itemToBlock.containsKey(item) ? itemToBlock.get(item) : item);
	}
	
	private byte getDataByItem(Material item) {
		return (itemToBlockDamage.containsKey(item) ? itemToBlockDamage.get(item).byteValue() : 0);
	}
	private String getMinecraftPackageName() {
		for (Package pack: Package.getPackages()) {
			if(pack.getName().startsWith("net.minecraft.server.v")) {
				String[] packs = pack.getName().split("\\.");
				return packs[0] + "." + packs[1] + "." + packs[2] + "." + packs[3];
			}
		}
		return null;
	}
	
	private Class getClass(String name) {
		try {
			return Class.forName(name);
		} catch (ClassNotFoundException ex) {
			return null;
		}
	}
	
	private Field getField(Class c, String field) {
		try {
			return c.getDeclaredField(field);
		} catch (NoSuchFieldException ex) {
			return null;
		}
	}
	
	private Method getMethod(Class c, String method, Class... argsType) {
		try {
			return c.getDeclaredMethod(method, argsType);
		} catch (NoSuchMethodException ex) {
			return null;
		}
	}
	
	private Object getFieldValue(Field field, Object obj) {
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
	
	private void setFieldValue(String field, Object obj, Object value) {
		setFieldValue(getField(obj.getClass(), field), obj, value);
	}
	private void setFieldValue(Field field, Object obj, Object value) {
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
	
	private Object invokeMethod(Method method, Object obj, Object... args) {
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
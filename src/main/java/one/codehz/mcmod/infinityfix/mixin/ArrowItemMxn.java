package one.codehz.mcmod.infinityfix.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArrowItem.class)
public class ArrowItemMxn {
  private static int getInfinityCount(LivingEntity shooter, int level, int bound) {
    if (level <= 0) return 0;
    int rnd = shooter.getRandom().nextInt(bound);
    int value = level - rnd;
    int result = (int) Math.ceil((float) value / (float) bound);
    return Math.max(result, 0);
  }

  @Inject(at = @At("TAIL"), method = "createArrow")
  void createArrow(World world, ItemStack stack, LivingEntity shooter, CallbackInfoReturnable<PersistentProjectileEntity> cir) {
    if (shooter instanceof PlayerEntity && ((PlayerEntity) shooter).isCreative()) {
      return;
    }
    int bound = 6;
    Item arrow_item = stack.getItem();
    if (arrow_item == Items.SPECTRAL_ARROW || arrow_item == Items.TIPPED_ARROW) {
      bound = 18;
    }
    // Invert the level to avoid conflict with vanilla infinity
    int raw_level = EnchantmentHelper.getLevel(Enchantments.INFINITY, shooter.getActiveItem());
    int level = -raw_level;
    int count = getInfinityCount(shooter, level, bound);
    if (count > 0) {
      cir.getReturnValue().pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
      ItemStack dropping_arrows = new ItemStack(arrow_item, count);
      shooter.dropStack(dropping_arrows);
    }
  }
}

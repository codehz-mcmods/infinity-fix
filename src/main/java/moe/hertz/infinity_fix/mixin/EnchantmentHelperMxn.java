package moe.hertz.infinity_fix.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.InfinityEnchantment;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMxn {
  @Inject(method = "getLevel", at = @At("RETURN"), cancellable = true)
  private static void getLevel(Enchantment enchantment, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
    if (enchantment instanceof InfinityEnchantment) {
      int value = cir.getReturnValue();
      cir.setReturnValue(-value);
    }
  }
}

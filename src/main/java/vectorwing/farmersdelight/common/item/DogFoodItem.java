package vectorwing.farmersdelight.common.item;

import com.google.common.collect.Lists;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.Configuration;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.registry.ModParticleTypes;
import vectorwing.farmersdelight.common.tag.ModTags;
import vectorwing.farmersdelight.common.utility.MathUtils;
import vectorwing.farmersdelight.common.utility.TextUtils;

import java.util.List;

;

public class DogFoodItem extends ConsumableItem
{
	public static final List<MobEffectInstance> EFFECTS = Lists.newArrayList(
			new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 6000, 0),
			new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 0),
			new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 0));

	public DogFoodItem(Properties properties) {
		super(properties);
	}

	public static void init(){
		UseEntityCallback.EVENT.register(DogFoodItem.DogFoodEvent::onDogFoodApplied);
	}

	public static class DogFoodEvent
	{

		public static InteractionResult onDogFoodApplied(Player player, Level level, InteractionHand hand, Entity target,
														 @Nullable EntityHitResult entityHitResult) {
			if (player.isSpectator()) return InteractionResult.PASS;

			ItemStack itemStack = player.getItemInHand(hand);

			if (target instanceof LivingEntity entity && target.getType().is(ModTags.DOG_FOOD_USERS)) {
				boolean isTameable = entity instanceof TamableAnimal;

				if (entity.isAlive() && (!isTameable || ((TamableAnimal) entity).isTame()) && itemStack.getItem().equals(ModItems.DOG_FOOD.get())) {
					entity.setHealth(entity.getMaxHealth());
					for (MobEffectInstance effect : EFFECTS) {
						entity.addEffect(new MobEffectInstance(effect));
					}
					entity.level().playSound(null, target.blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 0.8F, 0.8F);

					for (int i = 0; i < 5; ++i) {
						double xSpeed = MathUtils.RAND.nextGaussian() * 0.02D;
						double ySpeed = MathUtils.RAND.nextGaussian() * 0.02D;
						double zSpeed = MathUtils.RAND.nextGaussian() * 0.02D;
						entity.level().addParticle(ModParticleTypes.STAR.get(), entity.getRandomX(1.0D), entity.getRandomY() + 0.5D, entity.getRandomZ(1.0D), xSpeed, ySpeed, zSpeed);
					}

					if (itemStack.getRecipeRemainder() != ItemStack.EMPTY && !player.isCreative()) {
						player.addItem(itemStack.getRecipeRemainder());
						itemStack.shrink(1);
					}

					return InteractionResult.sidedSuccess(level.isClientSide);
				}
			}
			return InteractionResult.PASS;
		}

	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag isAdvanced) {
		if (!Configuration.FOOD_EFFECT_TOOLTIP.get()) {
			return;
		}

		MutableComponent textWhenFeeding = TextUtils.getTranslation("tooltip.dog_food.when_feeding");
		tooltip.add(textWhenFeeding.withStyle(ChatFormatting.GRAY));

		for (MobEffectInstance effectInstance : EFFECTS) {
			MutableComponent effectDescription = Component.literal(" ");
			MutableComponent effectName = Component.translatable(effectInstance.getDescriptionId());
			effectDescription.append(effectName);
			MobEffect effect = effectInstance.getEffect();

			if (effectInstance.getAmplifier() > 0) {
				effectDescription.append(" ").append(Component.translatable("potion.potency." + effectInstance.getAmplifier()));
			}

			if (effectInstance.getDuration() > 20) {
				effectDescription.append(" (").append(MobEffectUtil.formatDuration(effectInstance, 1.0F)).append(")");
			}

			tooltip.add(effectDescription.withStyle(effect.getCategory().getTooltipFormatting()));
		}
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player playerIn, LivingEntity target, InteractionHand hand) {
		if (target instanceof Wolf wolf) {
			if (wolf.isAlive() && wolf.isTame()) {
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.PASS;
	}
}

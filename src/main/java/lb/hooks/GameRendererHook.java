package lb.hooks;

import lb.Wrapper;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.function.Predicate;

import static lb.LB.inflatingNumber;

public class GameRendererHook extends GameRenderer implements Wrapper {
    public GameRendererHook() {
        super(minecraft, minecraft.getResourceManager(), minecraft.renderBuffers());
    }

    public static EntityRayTraceResult getEntityHitResult(Entity entity, Vector3d startVec, Vector3d endVec, AxisAlignedBB boundingBox, Predicate<Entity> filter, double maxDistanceSq) {
        World world = entity.level;
        double closestDistanceSq = maxDistanceSq;
        Entity closestEntity = null;
        Vector3d hitVec = null;

        for (Entity potentialTarget : world.getEntities(entity, boundingBox, filter)) {
            AxisAlignedBB targetBB = potentialTarget.getBoundingBox().inflate(potentialTarget.getPickRadius() + inflatingNumber);
            Optional<Vector3d> intercept = targetBB.clip(startVec, endVec);

            if (targetBB.contains(startVec)) {
                if (closestDistanceSq < 0.0) continue;
                closestEntity = potentialTarget;
                hitVec = intercept.orElse(startVec);
                closestDistanceSq = 0.0;
            } else if (intercept.isPresent()) {
                Vector3d interceptedPos = intercept.get();
                double distanceSq = startVec.distanceToSqr(interceptedPos);

                if ((distanceSq < closestDistanceSq || closestDistanceSq == 0.0) && (potentialTarget.getRootVehicle() != entity.getRootVehicle() || potentialTarget.canRiderInteract())) {
                    closestEntity = potentialTarget;
                    hitVec = interceptedPos;
                    closestDistanceSq = distanceSq;
                }
            }
        }

        return closestEntity != null ? new EntityRayTraceResult(closestEntity, hitVec) : null;
    }

    @Override
    public void pick(float partialTicks) {
        Entity viewer = minecraft.getCameraEntity();

        if (viewer != null && minecraft.level != null) {
            minecraft.getProfiler().push("pick");
            minecraft.crosshairPickEntity = null;

            double pickRange = minecraft.gameMode.getPickRange();
            minecraft.hitResult = viewer.pick(pickRange, partialTicks, false);
            Vector3d eyePos = viewer.getEyePosition(partialTicks);
            boolean isLongPick = pickRange > 3.0D;

            double maxDistanceSq = isLongPick ? 36.0D : pickRange * pickRange;
            Vector3d lookVec = viewer.getViewVector(1.0F);
            Vector3d endVec = eyePos.add(lookVec.x * pickRange, lookVec.y * pickRange, lookVec.z * pickRange);

            AxisAlignedBB bb = viewer.getBoundingBox().expandTowards(lookVec.scale(pickRange)).inflate(1.0D);
            EntityRayTraceResult entityHit = getEntityHitResult(viewer, eyePos, endVec, bb,
                    (entity) -> !entity.isSpectator() && entity.isPickable(), maxDistanceSq);

            if (entityHit != null) {
                Entity targetEntity = entityHit.getEntity();
                Vector3d hitPos = entityHit.getLocation();
                double distanceSq = eyePos.distanceToSqr(hitPos);

                if (isLongPick && distanceSq > 9.0D) {
                    minecraft.hitResult = net.minecraft.util.math.BlockRayTraceResult.miss(hitPos, net.minecraft.util.Direction.getNearest(lookVec.x, lookVec.y, lookVec.z), new net.minecraft.util.math.BlockPos(hitPos));
                } else if (distanceSq < maxDistanceSq || minecraft.hitResult == null) {
                    minecraft.hitResult = entityHit;
                    if (targetEntity instanceof LivingEntity || targetEntity instanceof ItemFrameEntity) {
                        minecraft.crosshairPickEntity = targetEntity;
                    }
                }
            }

            minecraft.getProfiler().pop();
        }
    }
}

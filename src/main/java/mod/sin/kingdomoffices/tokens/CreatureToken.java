package mod.sin.kingdomoffices.tokens;

import java.io.IOException;

import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;

public class CreatureToken extends ItemModTemplate implements ItemTypes, MiscConstants {
	private String name = "creature token";
	public void createTemplate() throws IOException{
		ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("wyvern.creaturetoken");
		itemBuilder.name(name, name+"s", "A token from a creature. Perhaps it can be worked with a hammer into something useful.");
		itemBuilder.itemTypes(new short[]{
				ITEM_TYPE_MAGIC
		});
		itemBuilder.imageNumber((short) 1209);
		itemBuilder.behaviourType((short) 1);
		itemBuilder.combatDamage(0);
		itemBuilder.decayTime(Long.MAX_VALUE);
		itemBuilder.dimensions(1, 1, 1);
		itemBuilder.primarySkill((int) NOID);
		itemBuilder.bodySpaces(EMPTY_BYTE_PRIMITIVE_ARRAY);
		itemBuilder.modelName("model.magic.orb.");
		itemBuilder.difficulty(5.0f);
		itemBuilder.weightGrams(10);
		itemBuilder.material((byte)20);
		itemBuilder.value(100);
		itemBuilder.isTraded(false);
		
		ItemTemplate template = itemBuilder.build();
		templateId = template.getTemplateId();
		logger.info(name+" TemplateID: "+templateId);
	}
}

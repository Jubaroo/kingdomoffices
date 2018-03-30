package mod.sin.kingdomoffices.tokens;

import java.io.IOException;

import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.CreationCategories;
import com.wurmonline.server.items.CreationEntryCreator;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.skills.SkillList;

public class JesterToken extends ItemModTemplate implements ItemTypes, MiscConstants {
	private String name = "jester token";
	public void createTemplate() throws IOException{
		ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.item.token.jester");
		itemBuilder.name(name, name+"s", "A jester token. This can be consumed to bid towards the Royal Jest office.");
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
		itemBuilder.difficulty(80.0f);
		itemBuilder.weightGrams(10);
		itemBuilder.material((byte)20);
		itemBuilder.value(100);
		itemBuilder.isTraded(false);
		
		ItemTemplate template = itemBuilder.build();
		templateId = template.getTemplateId();
		logger.info(name+" TemplateID: "+templateId);
	}

	public void initCreationEntry(){
		logger.info("initCreationEntry()");
		if(templateId > 0){
			logger.info("Creating "+name+" creation entry, ID = "+templateId);
			CreationEntryCreator.createSimpleEntry(SkillList.PUPPETEERING, ItemList.hammerMetal, 22765,
					templateId, false, true, 0f, false, false, CreationCategories.PRODUCTION);
		}else{
			logger.info(name+" does not have a template ID on creation entry.");
		}
	}
}

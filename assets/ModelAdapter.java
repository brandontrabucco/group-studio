package com.brandon.apps.groupstudio.assets;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.brandon.apps.groupstudio.R;
import com.brandon.apps.groupstudio.models.AttributeModel;
import com.brandon.apps.groupstudio.models.CalculationModel;
import com.brandon.apps.groupstudio.models.GroupModel;
import com.brandon.apps.groupstudio.models.MemberModel;
import com.brandon.apps.groupstudio.models.TypeModel;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

/**
 * Created by Brandon on 12/17/2015.
 */
public class ModelAdapter {
    public void OnUpdate(){}
    public GroupModel toModel(ActionBarActivity activity, DefaultGroup parentGroup) {
        final DatabaseAdapter database = new DatabaseAdapter(activity.getApplicationContext());
        database.open();

        GroupModel groupModel = new GroupModel(parentGroup.getId(),
                parentGroup.getName(),
                parentGroup.getDesc());

        for (DefaultMember member:
                database.getAllMembers(parentGroup.getId())) {
            MemberModel memberModel = new MemberModel(member.getId(),
                    member.getTypeId(),
                    member.getName());

            boolean exists = false;

            for (TypeModel type:
                    groupModel.TypeList) {
                if (type.TypeId == memberModel.MemberType) exists = true;
            }

            if (!exists) {
                DefaultType type = database.selectTypeById(member.getTypeId());
                TypeModel typeModel = new TypeModel(type.getId(), type.getName());

                for (DefaultAttribute attribute:
                        database.getAllTypeAttributes(type.getId())) {
                    AttributeModel attributeModel = new AttributeModel(attribute.getId(),
                            attribute.getUniversalId(),
                            attribute.getTypeId(),
                            attribute.getStatId(),
                            attribute.getRankId(),
                            attribute.getTitle(),
                            attribute.getValue());
                    typeModel.AttributeList.add(attributeModel);
                }

                for (Calculation calc:
                        database.getAllCalculations(type.getId())) {
                    CalculationModel calculationModel = new CalculationModel(calc.getId(),
                            calc.getTargetId(),
                            calc.getStatId(),
                            calc.getName());
                    typeModel.CalculationList.add(calculationModel);
                }

                groupModel.TypeList.add(typeModel);
            }

            for (DefaultAttribute attribute:
                    database.getAllMemberAttributes(parentGroup.getId(),
                            member.getId())) {
                AttributeModel attributeModel = new AttributeModel(attribute.getId(),
                        attribute.getUniversalId(),
                        attribute.getTypeId(),
                        attribute.getStatId(),
                        attribute.getRankId(),
                        attribute.getTitle(),
                        attribute.getValue());
                memberModel.AttributeList.add(attributeModel);
            }
            groupModel.MemberList.add(memberModel);
        }
        database.close();
        return groupModel;
    }
    // potential conflict when updating and creating member attributes. check universal id link with type attribute, might be creating extra attributes
    public void fromModel(final ActionBarActivity activity, final GroupModel model, final int overwriteId) {
        final DatabaseAdapter database = new DatabaseAdapter(activity.getApplicationContext());
        database.open();
        if (overwriteId != -1 && overwriteId != -2) {
            // update selected
            int groupId = overwriteId;
            DefaultGroup group = new DefaultGroup(groupId, model.GroupName, model.GroupDesc);
            database.updateGroupById(groupId, group.getName(), group.getDesc());
            for (TypeModel typeModel :
                    model.TypeList) {
                if (!database.typeExists(typeModel.TypeName)) {
                    // create type
                    int typeId = (int) database.insertType(new DefaultType(typeModel.TypeId, typeModel.TypeName));
                    for (AttributeModel attributeModel :
                            typeModel.AttributeList) {
                        // will only ever create attributes on new type
                        DefaultAttribute attribute = new DefaultAttribute(
                                attributeModel.AttributeId,
                                attributeModel.AttributeType,
                                attributeModel.AttributeRank,
                                attributeModel.AttributeTitle,
                                attributeModel.AttributeValue
                        );
                        attribute.setStatId(attributeModel.AttributeStat);
                        attribute.setUniversalId(attributeModel.AttributeUniversalId);
                        database.insertTypeAttribute(typeId, attribute);
                    }

                    for (CalculationModel calcModel :
                            typeModel.CalculationList) {
                        // will only ever create calculations on new type
                        int targetId = calcModel.CalculationTarget;
                        for (AttributeModel attribute :
                                typeModel.AttributeList) {
                            if (attribute.AttributeId == targetId) {
                                targetId = database.selectTypeAttributeIdByTitle(typeId, attribute.AttributeTitle);
                            }
                        }
                        Calculation calculation = new Calculation(
                                calcModel.CalculationId,
                                targetId,
                                calcModel.CalculationStat,
                                calcModel.CalculationName
                        );
                        database.insertCalculation(typeId, calculation);
                    }
                } else {
                    // update type
                    int typeId = database.selectTypeIdByName(typeModel.TypeName);
                    System.out.println("typeId: " + typeId);
                    // equals zero error, no such id
                    database.updateTypeById(
                            typeId,
                            typeModel.TypeName
                    );
                    for (AttributeModel attributeModel :
                            typeModel.AttributeList) {
                        if (!database.typeAttributeExists(typeId, attributeModel.AttributeTitle)) {
                            // create attribute
                            DefaultAttribute attribute = new DefaultAttribute(
                                    attributeModel.AttributeId,
                                    attributeModel.AttributeType,
                                    attributeModel.AttributeRank,
                                    attributeModel.AttributeTitle,
                                    attributeModel.AttributeValue
                            );
                            attribute.setStatId(attributeModel.AttributeStat);
                            attribute.setUniversalId(attributeModel.AttributeUniversalId);
                            database.insertTypeAttribute(typeId, attribute);
                        } else {
                            // update attribute
                            database.updateTypeAttributeById(
                                    typeId,
                                    database.selectTypeAttributeIdByTitle(typeId, attributeModel.AttributeTitle),
                                    attributeModel.AttributeRank,
                                    attributeModel.AttributeStat,
                                    attributeModel.AttributeTitle,
                                    attributeModel.AttributeValue
                            );
                        }
                    }
                    for (CalculationModel calcModel :
                            typeModel.CalculationList) {
                        int targetId = calcModel.CalculationTarget;
                        for (AttributeModel attribute :
                                typeModel.AttributeList) {
                            if (attribute.AttributeId == targetId) {
                                targetId = database.selectTypeAttributeIdByTitle(typeId, attribute.AttributeTitle);
                            }
                        }
                        if (!database.calculationExists(typeId, calcModel.CalculationName)) {
                            // create calculation
                            Calculation calculation = new Calculation(
                                    calcModel.CalculationId,
                                    targetId,
                                    calcModel.CalculationStat,
                                    calcModel.CalculationName
                            );
                            database.insertCalculation(typeId, calculation);
                        } else {
                            // update calculation
                            database.updateCalculationById(
                                    typeId,
                                    database.selectCalculationIdByName(typeId, calcModel.CalculationName),
                                    targetId,
                                    calcModel.CalculationStat,
                                    calcModel.CalculationName
                            );
                        }
                    }
                }
            }

            for (MemberModel memberModel :
                    model.MemberList) {
                if (!database.memberExists(groupId, memberModel.MemberName)) {
                    // create member
                    DefaultMember member = new DefaultMember(
                            memberModel.MemberId,
                            memberModel.MemberName,
                            memberModel.MemberType, // problem, need to get actual typeid
                            model.GroupId
                    );
                    for (TypeModel typeModel :
                            model.TypeList) {
                        if (member.getTypeId() == typeModel.TypeId) {
                            member.setTypeId(database.selectTypeIdByName(typeModel.TypeName));
                        }
                    }
                    int memberId = (int) database.insertMember(group, member);
                    int typeId = member.getTypeId();
                    for (AttributeModel attributeModel :
                            memberModel.AttributeList) {
                        // will only ever create attributes on new member
                        int universalId = attributeModel.AttributeUniversalId;
                        for (TypeModel typeModel :
                                model.TypeList) {
                            if (typeModel.TypeId == memberModel.MemberType) {
                                for (AttributeModel attribute :
                                        typeModel.AttributeList) {
                                    if (attribute.AttributeId == universalId) {
                                        // set the universal id to the device specific idea, which is subject to be different than the cloud id
                                        universalId = database.selectTypeAttributeIdByTitle(typeId, attribute.AttributeTitle);
                                    }
                                }
                            }
                        }
                        DefaultAttribute attribute = new DefaultAttribute(
                                attributeModel.AttributeId,
                                attributeModel.AttributeType,
                                attributeModel.AttributeRank,
                                attributeModel.AttributeTitle,
                                attributeModel.AttributeValue
                        );
                        attribute.setStatId(attributeModel.AttributeStat);
                        attribute.setUniversalId(universalId);
                        database.insertMemberAttribute(groupId, memberId, attribute);
                    }
                } else {
                    // update member
                    int memberId = database.selectMemberIdByName(groupId, memberModel.MemberName);
                    int typeId = memberModel.MemberType;
                    for (TypeModel typeModel :
                            model.TypeList) {
                        if (memberModel.MemberType == typeModel.TypeId) {
                            typeId = database.selectTypeIdByName(typeModel.TypeName);
                        }
                    }
                    database.updateMemberById(
                            groupId,
                            memberId,
                            memberModel.MemberName,
                            typeId
                    );

                    for (AttributeModel attributeModel :
                            memberModel.AttributeList) {
                        if (!database.memberAttributeExists(groupId, memberId, attributeModel.AttributeTitle)) {
                            // create attribute
                            int universalId = attributeModel.AttributeUniversalId;
                            for (TypeModel typeModel :
                                    model.TypeList) {
                                if (typeModel.TypeId == memberModel.MemberType) {
                                    for (AttributeModel attribute :
                                            typeModel.AttributeList) {
                                        if (attribute.AttributeId == universalId) {
                                            // set the universal id to the device specific idea, which is subject to be different than the cloud id
                                            universalId = database.selectTypeAttributeIdByTitle(typeId, attribute.AttributeTitle);
                                        }
                                    }
                                }
                            }
                            DefaultAttribute attribute = new DefaultAttribute(
                                    attributeModel.AttributeId,
                                    attributeModel.AttributeType,
                                    attributeModel.AttributeRank,
                                    attributeModel.AttributeTitle,
                                    attributeModel.AttributeValue
                            );
                            attribute.setStatId(attributeModel.AttributeStat);
                            attribute.setUniversalId(universalId);
                            database.insertMemberAttribute(groupId, memberId, attribute);
                        } else {
                            // update attribute
                            int universalId = attributeModel.AttributeUniversalId;
                            for (TypeModel typeModel :
                                    model.TypeList) {
                                if (typeModel.TypeId == memberModel.MemberType) {
                                    for (AttributeModel attribute :
                                            typeModel.AttributeList) {
                                        if (attribute.AttributeId == universalId) {
                                            // set the universal id to the device specific idea, which is subject to be different than the cloud id
                                            universalId = database.selectTypeAttributeIdByTitle(typeId, attribute.AttributeTitle);
                                        }
                                    }
                                }
                            }
                            database.updateMemberAttributeById(
                                    groupId,
                                    memberId,
                                    database.selectMemberAttributeIdByTitle(groupId, memberId, attributeModel.AttributeTitle),
                                    universalId,
                                    attributeModel.AttributeStat,
                                    attributeModel.AttributeTitle,
                                    attributeModel.AttributeValue
                            );
                        }
                    }
                }
            }
        } else if (overwriteId == -1) {
            // create new
            DefaultGroup group = new DefaultGroup(model.GroupId, model.GroupName, model.GroupDesc);
            int groupId = (int) database.insertGroup(group);
            group.setId(groupId);

            for (TypeModel typeModel :
                    model.TypeList) {
                if (!database.typeExists(typeModel.TypeName)) {
                    // create type
                    int typeId = (int) database.insertType(new DefaultType(typeModel.TypeId, typeModel.TypeName));
                    for (AttributeModel attributeModel :
                            typeModel.AttributeList) {
                        // will only ever create attributes on new type
                        DefaultAttribute attribute = new DefaultAttribute(
                                attributeModel.AttributeId,
                                attributeModel.AttributeType,
                                attributeModel.AttributeRank,
                                attributeModel.AttributeTitle,
                                attributeModel.AttributeValue
                        );
                        attribute.setStatId(attributeModel.AttributeStat);
                        attribute.setUniversalId(attributeModel.AttributeUniversalId);
                        database.insertTypeAttribute(typeId, attribute);
                    }

                    for (CalculationModel calcModel :
                            typeModel.CalculationList) {
                        // will only ever create calculations on new type
                        int targetId = calcModel.CalculationTarget;
                        for (AttributeModel attribute :
                                typeModel.AttributeList) {
                            if (attribute.AttributeId == targetId) {
                                targetId = database.selectTypeAttributeIdByTitle(typeId, attribute.AttributeTitle);
                            }
                        }
                        Calculation calculation = new Calculation(
                                calcModel.CalculationId,
                                targetId,
                                calcModel.CalculationStat,
                                calcModel.CalculationName
                        );
                        database.insertCalculation(typeId, calculation);
                    }
                } else {
                    // update type
                    int typeId = database.selectTypeIdByName(typeModel.TypeName);
                    database.updateTypeById(
                            typeId,
                            typeModel.TypeName
                    );
                    for (AttributeModel attributeModel :
                            typeModel.AttributeList) {
                        if (!database.typeAttributeExists(typeId, attributeModel.AttributeTitle)) {
                            // create attribute
                            DefaultAttribute attribute = new DefaultAttribute(
                                    attributeModel.AttributeId,
                                    attributeModel.AttributeType,
                                    attributeModel.AttributeRank,
                                    attributeModel.AttributeTitle,
                                    attributeModel.AttributeValue
                            );
                            attribute.setStatId(attributeModel.AttributeStat);
                            attribute.setUniversalId(attributeModel.AttributeUniversalId);
                            database.insertTypeAttribute(typeId, attribute);
                        } else {
                            // update attribute
                            database.updateTypeAttributeById(
                                    typeId,
                                    database.selectTypeAttributeIdByTitle(typeId, attributeModel.AttributeTitle),
                                    attributeModel.AttributeRank,
                                    attributeModel.AttributeStat,
                                    attributeModel.AttributeTitle,
                                    attributeModel.AttributeValue
                            );
                        }
                    }
                    for (CalculationModel calcModel :
                            typeModel.CalculationList) {
                        int targetId = calcModel.CalculationTarget;
                        for (AttributeModel attribute :
                                typeModel.AttributeList) {
                            if (attribute.AttributeId == targetId) {
                                targetId = database.selectTypeAttributeIdByTitle(typeId, attribute.AttributeTitle);
                            }
                        }
                        if (!database.calculationExists(typeId, calcModel.CalculationName)) {
                            // create calculation
                            Calculation calculation = new Calculation(
                                    calcModel.CalculationId,
                                    targetId,
                                    calcModel.CalculationStat,
                                    calcModel.CalculationName
                            );
                            database.insertCalculation(typeId, calculation);
                        } else {
                            // update calculation
                            database.updateCalculationById(
                                    typeId,
                                    database.selectCalculationIdByName(typeId, calcModel.CalculationName),
                                    targetId,
                                    calcModel.CalculationStat,
                                    calcModel.CalculationName
                            );
                        }
                    }
                }
            }

            for (MemberModel memberModel :
                    model.MemberList) {
                // will only ever create members on a new group
                DefaultMember member = new DefaultMember(
                        memberModel.MemberId,
                        memberModel.MemberName,
                        memberModel.MemberType,
                        model.GroupId
                );

                for (TypeModel typeModel :
                        model.TypeList) {
                    if (member.getTypeId() == typeModel.TypeId) {
                        member.setTypeId(database.selectTypeIdByName(typeModel.TypeName));
                    }
                }

                int memberId = (int) database.insertMember(group, member);
                int typeId = member.getTypeId();

                for (AttributeModel attributeModel :
                        memberModel.AttributeList) {
                    // will only ever create attributes on a new member
                    int universalId = attributeModel.AttributeUniversalId;
                    for (TypeModel typeModel :
                            model.TypeList) {
                        if (typeModel.TypeId == memberModel.MemberType) {
                            for (AttributeModel attribute :
                                    typeModel.AttributeList) {
                                if (attribute.AttributeId == universalId) {
                                    // set the universal id to the device specific idea, which is subject to be different than the cloud id
                                    universalId = database.selectTypeAttributeIdByTitle(typeId, attribute.AttributeTitle);
                                }
                            }
                        }
                    }

                    DefaultAttribute attribute = new DefaultAttribute(
                            attributeModel.AttributeId,
                            attributeModel.AttributeType,
                            attributeModel.AttributeRank,
                            attributeModel.AttributeTitle,
                            attributeModel.AttributeValue
                    );
                    attribute.setStatId(attributeModel.AttributeStat);
                    attribute.setUniversalId(universalId);
                    database.insertMemberAttribute(groupId, memberId, attribute);
                }
            }
        } else {
            Gson serializer = new Gson();
            String data = serializer.toJson(model);
            try {
                FileHandler.write(activity.getApplicationContext(), data, model.GroupName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Sync Complete!");
        if (overwriteId == -2) {
            Toast.makeText(activity.getApplicationContext(), "Saved to Documents!", Toast.LENGTH_SHORT).show();
        }
        database.close();
        OnUpdate();
    }
}

package com.actitime.demo.api;

import com.actimind.petri.model.DSL;
import com.actimind.petri.model.Network;
import com.actimind.petri.model.Place;

public class CPTNetwork extends Network {
    final Place empty = place("empty").oneTokenAllowed();
    final Place customerExists = place("customer exists").oneTokenAllowed();
    final Place projectExists = place("project exists").oneTokenAllowed();
    final Place customerIsArchived = place("existent customer is archived").oneTokenAllowed();
    final Place projectIsArchived = place("existent project is archived").oneTokenAllowed();

    {
        {
            transition("Create customer").from(empty).to(customerExists);
            transition("Destroy customer").from(customerExists).to(empty)
                    .reset(projectExists, projectIsArchived, customerIsArchived);

            //todo: archive project as well? another transition?
            transition("Archive customer")
                    .fromAndTo(customerExists)
                    .to(customerIsArchived, projectIsArchived);
            transition("Archive customer+archived project")
                    .fromAndTo(customerExists, projectExists)
                    .from(projectIsArchived)
                    .to(customerIsArchived, projectIsArchived);

            transition("Restore customer")
                    .from(customerExists, customerIsArchived)
                    //.reset(projectIsArchived)
                    .to(customerExists);

            transition("Create project")
                    .fromAndTo(customerExists)
                    .inhibitFrom(customerIsArchived)
                    .reset(projectIsArchived)
                    .to(projectExists);

            transition("Delete project")
                    .from(projectExists)
                    .reset(projectIsArchived);

            transition("Archive project")
                    .fromAndTo(projectExists)
                    .inhibitFrom(customerIsArchived, projectIsArchived)
                    .to(projectIsArchived);

            transition("Restore project")
                    .fromAndTo(projectExists)
                    .from(projectIsArchived)
                    .inhibitFrom(customerIsArchived);
            //todo: how to test expected errors?


            empty.addToken();
        }
    }

    public static void main(String[] args) {
        DSL.showUI(new CPTNetwork());
    }
}

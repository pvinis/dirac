/*
 * Copyright (C) 2009 280 North Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY APPLE INC. ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL APPLE INC. OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * @constructor
 * @extends {WebInspector.ProfileDataGridNode}
 * @param {!WebInspector.ProfileNode} profileNode
 * @param {!WebInspector.TopDownProfileDataGridTree} owningTree
 */
WebInspector.TopDownProfileDataGridNode = function(profileNode, owningTree)
{
    var hasChildren = !!(profileNode.children && profileNode.children.length);

    WebInspector.ProfileDataGridNode.call(this, profileNode, owningTree, hasChildren);

    this._remainingChildren = profileNode.children;
}

WebInspector.TopDownProfileDataGridNode.prototype = {
    /**
     * @override
     */
    populateChildren: function()
    {
        WebInspector.TopDownProfileDataGridNode._sharedPopulate(this);
    },

    __proto__: WebInspector.ProfileDataGridNode.prototype
}

/**
 * @param {!WebInspector.TopDownProfileDataGridNode|!WebInspector.TopDownProfileDataGridTree} container
 */
WebInspector.TopDownProfileDataGridNode._sharedPopulate = function(container)
{
    var children = container._remainingChildren;
    var childrenLength = children.length;

    for (var i = 0; i < childrenLength; ++i)
        container.appendChild(new WebInspector.TopDownProfileDataGridNode(children[i], /** @type {!WebInspector.TopDownProfileDataGridTree} */(container.tree)));

    container._remainingChildren = null;
}

/**
 * @param {!WebInspector.TopDownProfileDataGridNode|!WebInspector.TopDownProfileDataGridTree} container
 * @param {string} aCallUID
 */
WebInspector.TopDownProfileDataGridNode._excludeRecursively = function(container, aCallUID)
{
    if (container._remainingChildren)
        container.populate();

    container.save();

    var children = container.children;
    var index = container.children.length;

    while (index--)
        WebInspector.TopDownProfileDataGridNode._excludeRecursively(children[index], aCallUID);

    var child = container.childrenByCallUID.get(aCallUID);

    if (child)
        WebInspector.ProfileDataGridNode.merge(container, child, true);
}

/**
 * @constructor
 * @extends {WebInspector.ProfileDataGridTree}
 * @param {!WebInspector.ProfileDataGridNode.Formatter} formatter
 * @param {!WebInspector.SearchableView} searchableView
 * @param {!WebInspector.ProfileNode} rootProfileNode
 * @param {number} total
 */
WebInspector.TopDownProfileDataGridTree = function(formatter, searchableView, rootProfileNode, total)
{
    WebInspector.ProfileDataGridTree.call(this, formatter, searchableView, total);
    this._remainingChildren = rootProfileNode.children;
    WebInspector.ProfileDataGridNode.populate(this);
}

WebInspector.TopDownProfileDataGridTree.prototype = {
    /**
     * @param {!WebInspector.ProfileDataGridNode} profileDataGridNode
     */
    focus: function(profileDataGridNode)
    {
        if (!profileDataGridNode)
            return;

        this.save();
        profileDataGridNode.savePosition();

        this.children = [profileDataGridNode];
        this.total = profileDataGridNode.total;
    },

    /**
     * @param {!WebInspector.ProfileDataGridNode} profileDataGridNode
     */
    exclude: function(profileDataGridNode)
    {
        if (!profileDataGridNode)
            return;

        this.save();

        WebInspector.TopDownProfileDataGridNode._excludeRecursively(this, profileDataGridNode.callUID);

        if (this.lastComparator)
            this.sort(this.lastComparator, true);
    },

    restore: function()
    {
        if (!this._savedChildren)
            return;

        this.children[0].restorePosition();

        WebInspector.ProfileDataGridTree.prototype.restore.call(this);
    },

    /**
     * @override
     */
    populateChildren: function()
    {
        WebInspector.TopDownProfileDataGridNode._sharedPopulate(this);
    },

    __proto__: WebInspector.ProfileDataGridTree.prototype
}

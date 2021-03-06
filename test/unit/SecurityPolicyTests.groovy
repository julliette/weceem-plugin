import grails.test.*

import org.weceem.security.*
import org.weceem.blog.*
import org.weceem.content.*
import org.weceem.html.*

class SecurityPolicyTests extends grails.test.GrailsUnitTestCase {

    def policy
    
    void setUp() {
        super.setUp()
        
        policy = new WeceemSecurityPolicy()
    }

    void testDefaultRuntimePolicy() {
        policy.initDefault()
        
        assertTrue policy.hasPermission('weceem', '/anything', [WeceemSecurityPolicy.ROLE_ADMIN], [WeceemSecurityPolicy.PERMISSION_ADMIN])
        assertTrue policy.hasPermission('weceem', '/anything', [WeceemSecurityPolicy.ROLE_ADMIN], [WeceemSecurityPolicy.PERMISSION_EDIT])
        assertTrue policy.hasPermission('weceem', '/anything', [WeceemSecurityPolicy.ROLE_ADMIN], [WeceemSecurityPolicy.PERMISSION_VIEW])
        assertTrue policy.hasPermission('weceem', '/anything', [WeceemSecurityPolicy.ROLE_ADMIN], [WeceemSecurityPolicy.PERMISSION_ADMIN])
        assertTrue policy.hasPermission('weceem', '/anything', [WeceemSecurityPolicy.ROLE_ADMIN], [WeceemSecurityPolicy.PERMISSION_DELETE])

        assertFalse policy.hasPermission('weceem', '/anything', [WeceemSecurityPolicy.ROLE_USER], [WeceemSecurityPolicy.PERMISSION_ADMIN])
        assertTrue policy.hasPermission('weceem', '/anything', [WeceemSecurityPolicy.ROLE_USER], [WeceemSecurityPolicy.PERMISSION_EDIT])
        assertTrue policy.hasPermission('weceem', '/anything', [WeceemSecurityPolicy.ROLE_USER], [WeceemSecurityPolicy.PERMISSION_VIEW])
        assertFalse policy.hasPermission('weceem', '/anything', [WeceemSecurityPolicy.ROLE_USER], [WeceemSecurityPolicy.PERMISSION_ADMIN])
        assertTrue policy.hasPermission('weceem', '/anything', [WeceemSecurityPolicy.ROLE_USER], [WeceemSecurityPolicy.PERMISSION_DELETE])

        assertFalse policy.hasPermission('weceem', '/anything', [WeceemSecurityPolicy.ROLE_GUEST], [WeceemSecurityPolicy.PERMISSION_ADMIN])
        assertFalse policy.hasPermission('weceem', '/anything', [WeceemSecurityPolicy.ROLE_GUEST], [WeceemSecurityPolicy.PERMISSION_EDIT])
        assertTrue policy.hasPermission('weceem', '/anything', [WeceemSecurityPolicy.ROLE_GUEST], [WeceemSecurityPolicy.PERMISSION_VIEW])
        assertFalse policy.hasPermission('weceem', '/anything', [WeceemSecurityPolicy.ROLE_GUEST], [WeceemSecurityPolicy.PERMISSION_ADMIN])
        assertFalse policy.hasPermission('weceem', '/anything', [WeceemSecurityPolicy.ROLE_GUEST], [WeceemSecurityPolicy.PERMISSION_DELETE])
    }

    void testSpaceRoleDefaults() {
        policy.setDefaultPermissionForSpaceAndRole(WeceemSecurityPolicy.PERMISSION_VIEW, true, 'spaceA', "user")
        policy.setDefaultPermissionForSpaceAndRole(WeceemSecurityPolicy.PERMISSION_ADMIN, true, 'spaceB', "admin")
        policy.setDefaultPermissionForSpaceAndRole(WeceemSecurityPolicy.PERMISSION_VIEW, true, 'spaceB', "admin")
        policy.setDefaultPermissionForSpaceAndRole(WeceemSecurityPolicy.PERMISSION_VIEW, true, 'spaceA', "admin")
        
        assertTrue policy.hasPermission('spaceA', '/anything', ['user'], [WeceemSecurityPolicy.PERMISSION_VIEW])
        assertTrue policy.hasPermission('spaceA', '/anything', ['admin'], [WeceemSecurityPolicy.PERMISSION_VIEW])
        assertTrue policy.hasPermission('spaceB', '/anything', ['admin'], [WeceemSecurityPolicy.PERMISSION_VIEW])

        assertFalse policy.hasPermission('spaceB', '/anything', ['user'], [WeceemSecurityPolicy.PERMISSION_VIEW])
    }

    void testURIAndSpaceSpecificAccessControl() {
        policy.setURIPermissionForSpaceAndRole("/", WeceemSecurityPolicy.PERMISSION_ADMIN, true, 'spaceA', "spaceA_admin")
        policy.setURIPermissionForSpaceAndRole("/", WeceemSecurityPolicy.PERMISSION_EDIT, true, 'spaceA', "spaceA_admin")
        policy.setURIPermissionForSpaceAndRole("/", WeceemSecurityPolicy.PERMISSION_VIEW, true, 'spaceA', "spaceA_admin")
        policy.setURIPermissionForSpaceAndRole("/", WeceemSecurityPolicy.PERMISSION_CREATE, true, 'spaceA', "spaceA_admin")
        policy.setURIPermissionForSpaceAndRole("/", WeceemSecurityPolicy.PERMISSION_DELETE, true, 'spaceA', "spaceA_admin")

        policy.setURIPermissionForSpaceAndRole("/", WeceemSecurityPolicy.PERMISSION_VIEW, true, 'spaceA', "spaceA_guest")
        policy.setURIPermissionForSpaceAndRole("/blog/comments", WeceemSecurityPolicy.PERMISSION_CREATE, true, 'spaceA', "spaceA_guest")
        policy.setURIPermissionForSpaceAndRole("/blog", WeceemSecurityPolicy.PERMISSION_EDIT, true, 'spaceA', "spaceA_user")
        policy.setURIPermissionForSpaceAndRole("/blog", WeceemSecurityPolicy.PERMISSION_CREATE, true, 'spaceA', "spaceA_user")
        policy.setURIPermissionForSpaceAndRole("/extranet", WeceemSecurityPolicy.PERMISSION_VIEW, false, 'spaceA', "spaceA_guest")

        policy.setURIPermissionForSpaceAndRole("/", WeceemSecurityPolicy.PERMISSION_ADMIN, true, 'spaceB', "spaceB_admin")
        policy.setURIPermissionForSpaceAndRole("/", WeceemSecurityPolicy.PERMISSION_EDIT, true, 'spaceB', "spaceB_admin")
        policy.setURIPermissionForSpaceAndRole("/", WeceemSecurityPolicy.PERMISSION_VIEW, true, 'spaceB', "spaceB_admin")
        policy.setURIPermissionForSpaceAndRole("/", WeceemSecurityPolicy.PERMISSION_CREATE, true, 'spaceB', "spaceB_admin")
        policy.setURIPermissionForSpaceAndRole("/", WeceemSecurityPolicy.PERMISSION_DELETE, true, 'spaceB', "spaceB_admin")

        policy.setURIPermissionForSpaceAndRole("/", WeceemSecurityPolicy.PERMISSION_VIEW, true, 'spaceB', "spaceB_guest")
        policy.setURIPermissionForSpaceAndRole("/blog/comments", WeceemSecurityPolicy.PERMISSION_CREATE, true, 'spaceB', "spaceB_guest")
        policy.setURIPermissionForSpaceAndRole("/blog/comments", WeceemSecurityPolicy.PERMISSION_CREATE, false, 'spaceB', "spaceB_user")
        policy.setURIPermissionForSpaceAndRole("/blog", WeceemSecurityPolicy.PERMISSION_EDIT, true, 'spaceB', "spaceB_user")
        policy.setURIPermissionForSpaceAndRole("/blog", WeceemSecurityPolicy.PERMISSION_CREATE, true, 'spaceB', "spaceB_user")

        // First check spaces are independent
        def adminARoles = ['ROLE_ADMIN', 'spaceA_admin']
        def adminBRoles = ['ROLE_ADMIN', 'spaceB_admin']
        assertTrue policy.hasPermission('spaceA', '/anything', 
            adminARoles, [WeceemSecurityPolicy.PERMISSION_ADMIN])
        assertFalse policy.hasPermission('spaceB', '/anything', 
            adminARoles, [WeceemSecurityPolicy.PERMISSION_ADMIN])
        assertTrue policy.hasPermission('spaceB', '/anything', 
            adminBRoles, [WeceemSecurityPolicy.PERMISSION_ADMIN])
        assertFalse policy.hasPermission('spaceA', '/anything', 
            adminBRoles, [WeceemSecurityPolicy.PERMISSION_ADMIN])

        // Check that admin rights are across the whole tree
        assertTrue policy.hasPermission('spaceA', '/anything/really/really/deep', 
            adminARoles, [WeceemSecurityPolicy.PERMISSION_ADMIN])

        // Check that per-URI rights working for blog access
        assertFalse policy.hasPermission('spaceA', '/blog', 
            ['spaceA_guest'], [WeceemSecurityPolicy.PERMISSION_CREATE])
        assertTrue policy.hasPermission('spaceA', '/blog', 
            ['spaceA_user'], [WeceemSecurityPolicy.PERMISSION_CREATE])

        assertTrue policy.hasPermission('spaceA', '/blog/comments', 
            ['spaceA_guest'], [WeceemSecurityPolicy.PERMISSION_CREATE])
        assertTrue policy.hasPermission('spaceA', '/blog/comments', 
            ['spaceA_user'], [WeceemSecurityPolicy.PERMISSION_EDIT])

        // URI Inheritance - users can also create in comments, even though granted only on the parent 'blogs'
        assertTrue policy.hasPermission('spaceA', '/blog/comments', 
            ['spaceA_user'], [WeceemSecurityPolicy.PERMISSION_CREATE])

        // But this is explicitly forbidding in space B where users cannot CREATE comments but can edit them
        assertTrue policy.hasPermission('spaceB', '/blog/comments', 
            ['spaceB_guest'], [WeceemSecurityPolicy.PERMISSION_CREATE])
        assertFalse policy.hasPermission('spaceB', '/blog/comments', 
            ['spaceB_user'], [WeceemSecurityPolicy.PERMISSION_CREATE])
        assertTrue policy.hasPermission('spaceB', '/blog/comments', 
            ['spaceB_user'], [WeceemSecurityPolicy.PERMISSION_EDIT])

        // And anybody can view anything, except /extranet
        assertTrue policy.hasPermission('spaceA', '/some/really/deep/content', 
            ['spaceA_guest'], [WeceemSecurityPolicy.PERMISSION_VIEW])
        assertTrue policy.hasPermission('spaceA', '/', 
            ['spaceA_guest'], [WeceemSecurityPolicy.PERMISSION_VIEW])
        assertFalse policy.hasPermission('spaceA', '/extranet/something', 
            ['spaceA_guest'], [WeceemSecurityPolicy.PERMISSION_VIEW])
        assertFalse policy.hasPermission('spaceA', '/extranet', 
            ['spaceA_guest'], [WeceemSecurityPolicy.PERMISSION_VIEW])
    }
    
    void testRoleSpecificSpaceDefaultsDoNotAffectSpaceDefaultsOfOtherRoles() {
        policy.setDefaultPermissionForSpaceAndRole(WeceemSecurityPolicy.PERMISSION_ADMIN, true, 'spaceA', "admin")
        policy.setDefaultPermissionForSpaceAndRole(WeceemSecurityPolicy.PERMISSION_ADMIN, false, 'spaceA', "user")
        
        policy.dumpPermissions()
        
        assertFalse policy.hasPermission('spaceA', '/anything', ['user'], [WeceemSecurityPolicy.PERMISSION_ADMIN])
        assertTrue policy.hasPermission('spaceA', '/anything', ['admin'], [WeceemSecurityPolicy.PERMISSION_ADMIN])
    }
    
    void testRoleSpecificSpaceDefaultsDoNotAffectGlobalSpaceDefaultsOfOtherRoles() {    
        policy.setDefaultPermissionForSpaceAndRole(WeceemSecurityPolicy.PERMISSION_ADMIN, true, '*', "admin")
        policy.setDefaultPermissionForSpaceAndRole(WeceemSecurityPolicy.PERMISSION_ADMIN, false, 'site1', "user")
        
        policy.dumpPermissions()

        assertFalse policy.hasPermission('site1', '/anything', ['user'], [WeceemSecurityPolicy.PERMISSION_ADMIN])
        assertTrue policy.hasPermission('site1', '/anything', ['admin'], [WeceemSecurityPolicy.PERMISSION_ADMIN])
    }
    
    void testTypeSpecificAccessControl() {
        policy.setURIPermissionForSpaceAndRole("/", WeceemSecurityPolicy.PERMISSION_ADMIN, true, 'spaceA', "spaceA_admin")
        policy.setURIPermissionForSpaceAndRole("/", WeceemSecurityPolicy.PERMISSION_EDIT, true, 'spaceA', "spaceA_admin")
        policy.setURIPermissionForSpaceAndRole("/", WeceemSecurityPolicy.PERMISSION_VIEW, true, 'spaceA', "spaceA_admin")
        policy.setURIPermissionForSpaceAndRole("/", WeceemSecurityPolicy.PERMISSION_CREATE, true, 'spaceA', "spaceA_admin")
        policy.setURIPermissionForSpaceAndRole("/", WeceemSecurityPolicy.PERMISSION_DELETE, true, 'spaceA', "spaceA_admin")

        policy.setURIPermissionForSpaceAndRole("/", WeceemSecurityPolicy.PERMISSION_VIEW, true, 'spaceA', "spaceA_guest")
        policy.setURIPermissionForSpaceAndRole("/blog/comments", WeceemSecurityPolicy.PERMISSION_CREATE, 
            [types:[WcmComment]], 'spaceA', "spaceA_guest")
        policy.setURIPermissionForSpaceAndRole("/blog", WeceemSecurityPolicy.PERMISSION_EDIT, true, 'spaceA', "spaceA_user")
        policy.setURIPermissionForSpaceAndRole("/blog", WeceemSecurityPolicy.PERMISSION_CREATE, 
            [types:[WcmBlogEntry, WcmComment]], 'spaceA', "spaceA_user")
        policy.setURIPermissionForSpaceAndRole("/extranet", WeceemSecurityPolicy.PERMISSION_VIEW, false, 'spaceA', "spaceA_guest")

        // Don't allow GSP editing of HTML content
        policy.setURIPermissionForSpaceAndRole("/marketing", WeceemSecurityPolicy.PERMISSION_EDIT, 
            [types:[(WcmHTMLContent):[allowGSP:false]]], 'spaceA', "spaceA_user")
        policy.setURIPermissionForSpaceAndRole("/marketing", WeceemSecurityPolicy.PERMISSION_CREATE, 
            [types:[(WcmHTMLContent):[allowGSP:false]]], 'spaceA', "spaceA_user")

        policy.setURIPermissionForSpaceAndRole("/", WeceemSecurityPolicy.PERMISSION_ADMIN, true, 'spaceB', "spaceB_admin")
        policy.setURIPermissionForSpaceAndRole("/", WeceemSecurityPolicy.PERMISSION_EDIT, true, 'spaceB', "spaceB_admin")
        policy.setURIPermissionForSpaceAndRole("/", WeceemSecurityPolicy.PERMISSION_VIEW, true, 'spaceB', "spaceB_admin")
        policy.setURIPermissionForSpaceAndRole("/", WeceemSecurityPolicy.PERMISSION_CREATE, true, 'spaceB', "spaceB_admin")
        policy.setURIPermissionForSpaceAndRole("/", WeceemSecurityPolicy.PERMISSION_DELETE, true, 'spaceB', "spaceB_admin")

        policy.setURIPermissionForSpaceAndRole("/", WeceemSecurityPolicy.PERMISSION_VIEW, true, 'spaceB', "spaceB_guest")
        policy.setURIPermissionForSpaceAndRole("/blog/comments", WeceemSecurityPolicy.PERMISSION_CREATE, [types:[WcmComment]], 'spaceB', "spaceB_guest")
        policy.setURIPermissionForSpaceAndRole("/blog/comments", WeceemSecurityPolicy.PERMISSION_CREATE, false, 'spaceB', "spaceB_user")
        policy.setURIPermissionForSpaceAndRole("/blog", WeceemSecurityPolicy.PERMISSION_EDIT, [types:[WcmBlog, WcmComment]], 'spaceB', "spaceB_user")
        policy.setURIPermissionForSpaceAndRole("/blog", WeceemSecurityPolicy.PERMISSION_CREATE, true, 'spaceB', "spaceB_user")

        policy.dumpPermissions()
        
        // Check that per-URI rights working for blog access
        assertFalse policy.hasPermission('spaceA', '/blog', 
            ['spaceA_guest'], [WeceemSecurityPolicy.PERMISSION_CREATE])
        assertFalse policy.hasPermission('spaceA', '/blog', 
            ['spaceA_user'], [WeceemSecurityPolicy.PERMISSION_CREATE])
        assertTrue policy.hasPermission('spaceA', '/blog', 
            ['spaceA_user'], [WeceemSecurityPolicy.PERMISSION_CREATE], [type:WcmBlogEntry])

        assertFalse policy.hasPermission('spaceA', '/blog/comments', 
            ['spaceA_guest'], [WeceemSecurityPolicy.PERMISSION_CREATE])
        assertTrue policy.hasPermission('spaceA', '/blog/comments', 
            ['spaceA_guest'], [WeceemSecurityPolicy.PERMISSION_CREATE], [type:WcmComment])
        assertTrue policy.hasPermission('spaceA', '/blog/comments', 
            ['spaceA_user'], [WeceemSecurityPolicy.PERMISSION_EDIT], [type:WcmComment])

        // URI Inheritance - users can also create in comments, even though granted only on the parent 'blogs'
        assertFalse policy.hasPermission('spaceA', '/blog/comments', 
            ['spaceA_user'], [WeceemSecurityPolicy.PERMISSION_CREATE])
        assertTrue policy.hasPermission('spaceA', '/blog/comments', 
            ['spaceA_user'], [WeceemSecurityPolicy.PERMISSION_CREATE], [type:WcmBlogEntry])

        // Here the guest IS allowed to create only WcmComment so checking without a type must return false
        assertFalse policy.hasPermission('spaceB', '/blog/comments', 
            ['spaceB_guest'], [WeceemSecurityPolicy.PERMISSION_CREATE])
        // But checking WITH a permitted type returns true
        assertTrue policy.hasPermission('spaceB', '/blog/comments', 
            ['spaceB_guest'], [WeceemSecurityPolicy.PERMISSION_CREATE], [type:WcmComment])
        assertFalse policy.hasPermission('spaceB', '/blog/comments', 
            ['spaceB_user'], [WeceemSecurityPolicy.PERMISSION_CREATE])
        assertFalse policy.hasPermission('spaceB', '/blog/comments', 
            ['spaceB_user'], [WeceemSecurityPolicy.PERMISSION_CREATE], [type:WcmComment])
        assertFalse policy.hasPermission('spaceB', '/blog/comments', 
            ['spaceB_user'], [WeceemSecurityPolicy.PERMISSION_EDIT])
        assertTrue policy.hasPermission('spaceB', '/blog/comments', 
            ['spaceB_user'], [WeceemSecurityPolicy.PERMISSION_EDIT], [type:WcmComment])

        // Check user can create only HTML and only if allowGSP is false, but Admin can still create+edit anything
        assertFalse policy.hasPermission('spaceA', '/marketing', 
            ['spaceA_user'], [WeceemSecurityPolicy.PERMISSION_CREATE], [type:WcmComment])
        assertFalse policy.hasPermission('spaceA', '/marketing', 
            ['spaceA_user'], [WeceemSecurityPolicy.PERMISSION_CREATE], [type:WcmHTMLContent])
        assertTrue policy.hasPermission('spaceA', '/marketing', 
            ['spaceA_user'], [WeceemSecurityPolicy.PERMISSION_CREATE], [type:WcmHTMLContent, content:new WcmHTMLContent(allowGSP:false)])
        assertFalse policy.hasPermission('spaceA', '/marketing', 
            ['spaceA_user'], [WeceemSecurityPolicy.PERMISSION_CREATE], [type:WcmHTMLContent, content:new WcmHTMLContent(allowGSP:true)])
    }
    
    void testMissingPolicyCausesException() {
        shouldFail {
            policy.load('this_path_does_not_exist.groovy')
        }
    }

    
    void testLoadPolicyScript() {
        policy.load('test/files/testpolicy.groovy')
        /*
        // This is a test security policy
        policy = {
            "ROLE_ADMIN" {
                space '', 'test'

                admin true
                view true
                edit true
                delete true
                create true
            }

            "ROLE_USER" {
                space '', 'test'

                view true

                "/blog" {
                    edit true
                    create true
                }
            }


            "ROLE_TEST_GUEST" {
                space '', 'test'

                view true
                create types:[org.weceem.content.WcmComment]
            }

            "ROLE_GUEST" {
                space ''

                view true
            }
        }
        */

        policy.dumpPermissions()

        ['', 'test'].each { spc ->
            assertTrue policy.hasPermission(spc, '/anything', ['ROLE_ADMIN'], 
                [WeceemSecurityPolicy.PERMISSION_ADMIN,
                 WeceemSecurityPolicy.PERMISSION_CREATE,
                 WeceemSecurityPolicy.PERMISSION_EDIT,
                 WeceemSecurityPolicy.PERMISSION_DELETE,
                 WeceemSecurityPolicy.PERMISSION_VIEW])
        }
        ['', 'test'].each { spc ->
            assertTrue policy.hasPermission(spc, '/anything', ['ROLE_USER'], [WeceemSecurityPolicy.PERMISSION_VIEW])
            assertFalse policy.hasPermission(spc, '/anything', ['ROLE_USER'], [WeceemSecurityPolicy.PERMISSION_ADMIN])
            assertTrue policy.hasPermission(spc, '/blog/anything', ['ROLE_USER'], [WeceemSecurityPolicy.PERMISSION_VIEW])
            assertTrue policy.hasPermission(spc, '/blog/anything', ['ROLE_USER'], [WeceemSecurityPolicy.PERMISSION_EDIT])
            assertTrue policy.hasPermission(spc, '/blog/anything', ['ROLE_USER'], [WeceemSecurityPolicy.PERMISSION_CREATE])
            assertFalse policy.hasPermission(spc, '/blog/anything', ['ROLE_USER'], [WeceemSecurityPolicy.PERMISSION_DELETE])

            assertFalse policy.hasPermission(spc, '/blog/anything', ['ROLE_TEST_GUEST'], [WeceemSecurityPolicy.PERMISSION_CREATE], [type:WcmBlogEntry])
            assertFalse policy.hasPermission(spc, '/blog/anything', ['ROLE_TEST_GUEST'], [WeceemSecurityPolicy.PERMISSION_EDIT], [type:WcmBlogEntry])
            assertTrue policy.hasPermission(spc, '/blog/anything', ['ROLE_TEST_GUEST'], [WeceemSecurityPolicy.PERMISSION_CREATE], [type:WcmComment])
            assertTrue policy.hasPermission(spc, '/blog', ['ROLE_TEST_GUEST'], [WeceemSecurityPolicy.PERMISSION_CREATE], [type:WcmComment])
        }

        def spc = ''
        assertTrue policy.hasPermission(spc, '/anything', ['ROLE_GUEST'], [WeceemSecurityPolicy.PERMISSION_VIEW])
        assertFalse policy.hasPermission(spc, '/anything', ['ROLE_GUEST'], [WeceemSecurityPolicy.PERMISSION_ADMIN])
        assertFalse policy.hasPermission(spc, '/anything', ['ROLE_GUEST'], [WeceemSecurityPolicy.PERMISSION_CREATE])
        assertFalse policy.hasPermission(spc, '/anything', ['ROLE_GUEST'], [WeceemSecurityPolicy.PERMISSION_EDIT])
        assertFalse policy.hasPermission(spc, '/anything', ['ROLE_GUEST'], [WeceemSecurityPolicy.PERMISSION_DELETE])

        spc = 'test'
        assertFalse policy.hasPermission(spc, '/anything', ['ROLE_GUEST'], [WeceemSecurityPolicy.PERMISSION_VIEW])
        assertFalse policy.hasPermission(spc, '/anything', ['ROLE_GUEST'], [WeceemSecurityPolicy.PERMISSION_ADMIN])
        assertFalse policy.hasPermission(spc, '/anything', ['ROLE_GUEST'], [WeceemSecurityPolicy.PERMISSION_CREATE])
        assertFalse policy.hasPermission(spc, '/anything', ['ROLE_GUEST'], [WeceemSecurityPolicy.PERMISSION_EDIT])
        assertFalse policy.hasPermission(spc, '/anything', ['ROLE_GUEST'], [WeceemSecurityPolicy.PERMISSION_DELETE])
    }
}